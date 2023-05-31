import json
import os
from datetime import datetime

from flask import Flask, request, jsonify, g, make_response
from flask_login import LoginManager, login_user, login_required, current_user
from sqlalchemy import or_, and_
from werkzeug.utils import secure_filename

from constant import *
from db import *
from domain import *
from task import *
from chatterbot import ask_bot

app = Flask(__name__)
# os.urandom(16).hex()
app.secret_key = 'eff29c38c24e6a4cddaba113485e31b7'
login_manager = LoginManager(app)


@app.before_request
def before_request():
    g.session = my_database()
    return


@app.teardown_request
def teardown_request(exception):
    db_session = getattr(g, "session", None)
    if db_session is not None:
        db_session.close()
    return


@login_manager.user_loader
def load_user(id):
    return g.session.query(User).get(id)


@login_manager.unauthorized_handler
def unauthorized_handler():
    return jsonify(status="ERROR", message="Unauthorized")


@app.route("/login", methods=["POST"])
def login():
    username = request.form.get("username", "", type=str)
    pw = request.form.get("pw", "", type=str)
    user = g.session.query(User).filter(and_(User.username == username, User.pw == pw)).first()
    if not user:
        return jsonify(status="ERROR", message="Bad login")
    else:
        login_user(user)
        return jsonify(status="OK", user=User.serialize(user))


@app.route("/user", methods=["POST"])
def create_user():
    username = request.form.get("username", "", type=str)
    nickname = request.form.get("nickname", "", type=str)
    avatar = request.form.get("avatar", "", type=str)
    location = request.form.get("location", "", type=str)
    birth = request.form.get("birth", "", type=str)
    sex = request.form.get("sex", "", type=str)
    password = request.form.get("pw", "", type=str)
    signature = request.form.get("signature", "", type=str)
    if not username or not nickname or not avatar or not location or not birth or not sex \
            or not password or not signature:
        return jsonify(status="ERROR", message="Invalid parameters")
    else:
        if len(username) > 20:
            return jsonify(status="ERROR", message="Username too long")
        elif len(nickname) > 20:
            return jsonify(status="ERROR", message="Nickname too long")
        elif len(location) > 20:
            return jsonify(status="ERROR", message="Location too long")
        elif len(signature) > 20:
            return jsonify(status="ERROR", message="Signature too long")

        user = g.session.query(User).filter(User.username == username).first()
        if not user:
            new_user = User(username=username, nickname=nickname, avatar=avatar, location=location, birth=birth,
                            sex=sex, pw=password, signature=signature)
            g.session.add(new_user)
            g.session.commit()
            login_user(new_user)
            return jsonify(status="OK", user=User.serialize(new_user))
        else:
            return jsonify(status="ERROR", message="Username already exists")


@app.route("/user/<int:id>", methods=["GET"])
@login_required
def get_user(id):
    user = g.session.query(User).get(id)
    if user is None:
        return jsonify(status="ERROR", message="No such user")
    else:
        return jsonify(status="OK", user=User.serialize(user))


@app.route("/user/current", methods=["GET"])
@login_required
def get_user_current():
    return jsonify(status="OK", user=User.serialize(current_user))


@app.route("/user/<int:user_id>/<string:value_type>", methods=["PATCH"])
@login_required
def modify_user(user_id, value_type):
    if current_user.id != user_id:
        return jsonify(status="ERROR", message="Id not matched")
    else:
        if value_type == "nickname":
            nickname = request.form.get("nickname", "", type=str)
            if not nickname:
                return jsonify(status="ERROR", message="Invalid nickname")
            else:
                current_user.nickname = nickname
                g.session.commit()
                return jsonify(status="OK")
        if value_type == "location":
            location = request.form.get("location", "", type=str)
            if not location:
                return jsonify(status="ERROR", message="Invalid location")
            else:
                current_user.nickname = location
                g.session.commit()
                return jsonify(status="OK")
        if value_type == "signature":
            signature = request.form.get("signature", "", type=str)
            if not signature:
                return jsonify(status="ERROR", message="Invalid signature")
            else:
                current_user.signature = signature
                g.session.commit()
                return jsonify(status="OK")


@app.route("/friend", methods=["POST"])
@login_required
def new_friend():
    user_id = request.form.get("user_id", -1, type=int)
    friend_id = request.form.get("friend_id", -1, type=int)
    if user_id == -1 or friend_id == -1:
        return jsonify(status="ERROR", message="Invalid parameters")
    else:
        friend = g.session.query(Friend).filter(and_(Friend.user_id == user_id, Friend.friend_id == friend_id)).first()
        if not friend:
            f_status = FriendStatus.init
            friend = Friend(user_id=user_id, friend_id=friend_id, f_status=f_status)
            g.session.add(friend)
            g.session.commit()
            to_token = g.session.query(FCMToken.token).filter(FCMToken.user_id == friend_id).first()
            if not to_token[0]:
                return jsonify(status="ERROR", message="No token available")
            new_friend_application_notification(user_id, current_user.nickname, to_token[0])
            return jsonify(status="OK")
        elif friend.f_status == FriendStatus.disable:
            friend.f_status = FriendStatus.init
            g.session.commit()
            to_token = g.session.query(FCMToken.token).filter(FCMToken.user_id == friend_id).first()
            if not to_token[0]:
                return jsonify(status="ERROR", message="No token available")
            new_friend_application_notification(user_id, current_user.nickname, friend_id, to_token[0])
            return jsonify(status="OK")
        elif friend.f_status == FriendStatus.init:
            to_token = g.session.query(FCMToken.token).filter(FCMToken.user_id == friend_id).first()
            if not to_token[0]:
                return jsonify(status="ERROR", message="No token available")
            new_friend_application_notification(user_id, current_user.nickname, to_token[0])
            return jsonify(status="OK")
        else:
            return jsonify(status="ERROR", message="Already make friends")


@app.route("/friend", methods=["PATCH"])
@login_required
def modify_friend():
    user_id = request.form.get("user_id", -1, type=int)
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id not matched")
    friend_id = request.form.get("friend_id", -1, type=int)
    nickname = request.form.get("nickname", "", str)
    f_status = request.form.get("status", "", type=str)
    if friend_id == -1 or not f_status or not nickname:
        return jsonify(status="ERROR", message="Invalid parameters")
    inited_friend = g.session.query(Friend).filter(
        and_(Friend.user_id == friend_id, Friend.friend_id == user_id)).first()
    if inited_friend:
        confirm_friend = g.session.query(Friend).filter(
            and_(Friend.user_id == user_id, Friend.friend_id == friend_id)).first()
        if confirm_friend:
            confirm_friend.f_status = FriendStatus.enable
            confirm_friend.nickname = nickname
        else:
            confirm_friend = Friend(user_id=user_id, friend_id=friend_id, f_status=FriendStatus.enable,
                                    nickname=nickname)
            g.session.add(confirm_friend)
        inited_friend.f_status = FriendStatus.enable
        inited_friend.nickname = current_user.nickname
        g.session.commit()
        to_token = g.session.query(FCMToken.token).filter(FCMToken.user_id == friend_id).first()
        if to_token[0]:
            confirm_friend_notification(inited_friend, to_token[0])
        return jsonify(status="OK")
    else:
        return jsonify(status="ERROR", message="Post friend to init first")


@app.route("/user/friends/<int:user_id>", methods=["GET"])
@login_required
def get_friends(user_id):
    friends = g.session.query(Friend).filter(
        and_(Friend.user_id == user_id, Friend.f_status == FriendStatus.enable)).order_by(Friend.nickname).all()
    return jsonify(status="OK", friends=[Friend.serialize(friend) for friend in friends])


@app.route("/user/groups/<int:user_id>", methods=["GET"])
@login_required
def get_user_groups(user_id):
    groups = g.session.query(Group).join(UserGroup, Group.id == UserGroup.group_id).filter(
        and_(UserGroup.user_id == user_id, UserGroup.ug_status == UserGroupStatus.enable)).all()
    return jsonify(status="OK", groups=[Group.serialize(group) for group in groups])


@app.route("/group/users/<int:group_id>", methods=["GET"])
@login_required
def get_group_users(group_id):
    users = g.session.query(User).join(UserGroup, User.id == UserGroup.user_id).filter(
        and_(UserGroup.group_id == group_id, UserGroup.ug_status == UserGroupStatus.enable)).all()
    return jsonify(status="OK", users=[User.serialize(user) for user in users])


@app.route("/group", methods=["POST"])
@login_required
def new_group():
    nickname = request.form.get("nickname", "", type=str)
    avatar = request.form.get("avatar", "", type=str)
    announcement = request.form.get("announcement", "", type=str)
    creator = request.form.get("creator", -1, type=int)
    administrator = request.form.get("administrator", -1, type=int)
    if nickname == "" or avatar == "" or creator == -1 or administrator == -1:
        return jsonify(status="ERROR", message="Invalid parameters")
    else:
        group = Group(nickname=nickname, avatar=avatar, announcement=announcement, creator=creator,
                      administrator=administrator, member_number=1)
        g.session.add(group)
        group_id = g.session.flush()
        user_group = UserGroup(user_id=creator, group_id=group_id, ug_status=UserGroupStatus.enable)
        g.session.add(user_group)
        g.session.commit()
        return jsonify(status="OK")


@app.route("/communications/user/<int:user_id>", methods=["GET"])
@login_required
def get_communications(user_id):
    communications = []
    friends = g.session.query(Friend).filter(
        and_(Friend.user_id == user_id, Friend.f_status == FriendStatus.enable)).all()
    for friend in friends:
        message = g.session.query(Message).filter(
            or_(and_(Message.from_id == user_id, Message.dest_id == friend.friend_id),
                and_(Message.dest_id == user_id, Message.from_id == friend.friend_id))) \
            .filter(and_(Message.msg_type == MessageType.p2p, Message.m_status != MessageStatus.disable)) \
            .order_by(Message.create_date.desc()).first()
        if message:
            if message.content_type == MessageContentType.image:
                message.content = '[Picture]'
            elif message.content_type == MessageContentType.video:
                message.content = '[Video]'
            elif message.content_type == MessageContentType.audio:
                message.content = '[Audio]'
            user = g.session.query(User).get(friend.friend_id)
            communication = create_by_friend(friend, user, message)
            communications.append(communication)

    user_groups = g.session.query(UserGroup).filter(
        and_(UserGroup.user_id == user_id, UserGroup.ug_status == UserGroupStatus.enable)).all()
    for user_group in user_groups:
        message = g.session.query(Message).filter(
            and_(Message.dest_id == user_group.group_id, Message.msg_type == MessageType.group,
                 Message.m_status != MessageStatus.disable)) \
            .order_by(Message.create_date.desc()).first()
        if message:
            group = g.session.query(Group).get(user_group.group_id)

            if message.content_type == MessageContentType.image:
                message.content = '[Picture]'
            elif message.content_type == MessageContentType.video:
                message.content = '[Video]'
            elif message.content_type == MessageContentType.audio:
                message.content = '[Audio]'

            if message.from_id != user_id:
                sender = g.session.query(User).get(message.from_id)
                message.content = sender.nickname + " said: " + message.content
            else:
                message.content = "I said: " + message.content

            communication = create_by_group(group, message)
            communications.append(communication)

    if communications:
        communications.sort(key=lambda x: x.when, reverse=True)
    return jsonify(status="OK",
                   communications=[Communication.serialize(communication) for communication in communications])


@app.route("/messages/user/<int:user_id>", methods=["GET"])
@login_required
def get_messages(user_id):
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id match error")
    contact_id = request.args.get("contact_id", 0, type=int)
    contact_type = request.args.get("contact_type", "", type=str)
    page_index = request.args.get("page_index", -1, type=int)
    page_size = request.args.get("page_size", -1, type=int)
    if contact_id == 0 or not contact_type or page_index == -1 or page_size == -1:
        return jsonify(status="ERROR", message="Invalid parameters")

    if contact_type == MessageType.p2p:
        messages = g.session.query(Message).filter(or_(and_(Message.from_id == user_id, Message.dest_id == contact_id),
                                                       and_(Message.dest_id == user_id,
                                                            Message.from_id == contact_id))).filter(
            Message.msg_type == MessageType.p2p).filter(Message.m_status != MessageStatus.disable).order_by(
            Message.create_date.desc()).limit(page_size).offset((page_index - 1) * page_size).all()
        return jsonify(status="OK", messages=[Message.serialize(message) for message in messages])
    elif contact_type == MessageType.group:
        contact_id = -1 * contact_id
        members = g.session.query(User).join(UserGroup, User.id == UserGroup.user_id).filter(
            UserGroup.group_id == contact_id).filter(UserGroup.ug_status == UserGroupStatus.enable).all()
        messages = g.session.query(Message).filter(Message.from_id.in_([member.id for member in members])).filter(
            Message.dest_id == contact_id).filter(Message.msg_type == MessageType.group).filter(
            Message.m_status != MessageStatus.disable).order_by(Message.create_date.desc()).limit(page_size).offset(
            (page_index - 1) * page_size).all()
        return jsonify(status="OK", messages=[Message.serialize(message) for message in messages])


basedir = os.path.abspath(os.path.dirname(__file__))


@app.route("/message", methods=["POST"])
@login_required
def post_messages():
    from_id = request.form.get("from_id", -1, type=int)
    if from_id != current_user.id:
        return jsonify(status="ERROR", message="User id match error")
    dest_id = request.form.get("dest_id", -1, type=int)
    content = request.form.get("content", "", type=str)
    content_type = request.form.get("content_type", "", type=str)
    msg_type = request.form.get("msg_type", "", type=str)
    if content_type == MessageContentType.text:
        message = Message(from_id=from_id, dest_id=dest_id, content=content, content_type=content_type,
                          msg_type=msg_type, m_status=MessageStatus.deliver)
        g.session.add(message)
        g.session.commit()
    else:
        f = request.files['file']
        basepath = os.path.dirname(__file__)
        if content_type == MessageContentType.image:
            basepath = os.path.join(basepath, '', 'image')
        elif content_type == MessageContentType.video:
            basepath = os.path.join(basepath, '', 'video')
        else:
            basepath = os.path.join(basepath, '', 'audio')

        upload_path = os.path.join(basepath, '', secure_filename(f.filename))
        f.save(upload_path)
        message = Message(from_id=from_id, dest_id=dest_id, content=content, content_type=content_type,
                          msg_type=msg_type, m_status=MessageStatus.deliver)
        g.session.add(message)
        g.session.commit()

    to_token = g.session.query(FCMToken.token).filter(FCMToken.user_id == dest_id).first()
    if to_token:
        new_message_notification(from_id, current_user.nickname, content,
                                 message.create_date.strftime('%Y%m%d %H:%M:%S'), content_type,
                                 msg_type, to_token[0])
    return jsonify(status="OK")


@app.route("/file/<string:file_type>", methods=["GET"])
@login_required
def get_file(file_type):
    file_path = request.args.get("file_path", "", str)
    base_path = os.path.dirname(__file__)
    base_path = os.path.join(base_path, '', file_type)
    base_path = os.path.join(base_path, '', secure_filename(file_path))
    data = open(base_path, "rb").read()
    response = make_response(data)
    if file_type == "image" or file_type == "avatar":
        response.headers['Content-Type'] = 'image/png'
    return response


@app.route("/avatar", methods=["POST"])
@login_required
def post_avatar():
    user_id = request.form.get("user_id", -1, type=int)
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id match error")
    path = request.form.get("path", "", type=str)
    if not path:
        return jsonify(status="ERROR", message="Invalid path")

    f = request.files['file']
    basepath = os.path.dirname(__file__)
    basepath = os.path.join(basepath, '', 'avatar')
    upload_path = os.path.join(basepath, '', secure_filename(f.filename))
    f.save(upload_path)
    user = g.session.query(User).get(user_id)
    user.avatar = path
    g.session.commit()
    return jsonify(status="OK")


@app.route("/contact/<int:user_id>/<int:contact_id>/<string:contact_type>", methods=["PATCH"])
@login_required
def modify_contact(user_id, contact_id, contact_type):
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id match error")
    display = request.form.get("display", "", type=str)
    if contact_type == MessageType.p2p:
        friend = g.session.query(Friend).filter(Friend.user_id == user_id).filter(
            Friend.friend_id == contact_id).filter(Friend.f_status == FriendStatus.enable).first()
        if friend:
            friend.nickname = display
            g.session.commit()
            return jsonify(status="OK")
    else:
        contact_id = -1 * contact_id
        user_group = g.session.query(UserGroup).filter(UserGroup.user_id == user_id).filter(
            UserGroup.group_id == contact_id).filter(UserGroup.ug_status == UserGroupStatus.enable).first()
        if user_group:
            user_group.nickname = display
            g.session.commit()
            return jsonify(status="OK")


@app.route("/user/attribute/<string:condition>", methods=["GET"])
@login_required
def search_friends(condition):
    already = g.session.query(Friend.friend_id).filter(Friend.user_id == current_user.id).filter(
        Friend.f_status == FriendStatus.enable)

    people = g.session.query(User).filter(User.username.like(condition + '%')).filter(
        User.id != current_user.id).filter(User.id.not_in(already)).all()
    if people:
        return jsonify(status="OK", people=[User.serialize(person) for person in people])

    people = g.session.query(User).filter(User.nickname.like(condition + '%')).filter(
        User.id != current_user.id).filter(User.id.not_in(already)).all()
    if people:
        return jsonify(status="OK", people=[User.serialize(person) for person in people])
    people = g.session.query(User).filter(User.location.like(condition + '%')).filter(
        User.id != current_user.id).filter(User.id.not_in(already)).all()
    if people:
        return jsonify(status="OK", people=[User.serialize(person) for person in people])
    return jsonify(status="ERROR")


@app.route("/token", methods=["POST"])
@login_required
def save_fcm_token():
    user_id = request.form.get("user_id", -1, type=int)
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id not matched")
    token = request.form.get("token", "", type=str)
    if not token:
        return jsonify(status="ERROR", message="Invalid token")
    exist_token = g.session.query(FCMToken).filter(FCMToken.user_id == user_id).first()
    if not exist_token:
        new_token = FCMToken(user_id=user_id, token=token)
        g.session.add(new_token)
        g.session.commit()
        return jsonify(status="OK")
    elif exist_token.token == token:
        return jsonify(status="OK")
    else:
        exist_token.token = token
        g.session.commit()
        return jsonify(status="OK")


@app.route("/activity", methods=["POST"])
@login_required
def post_activity():
    post_form = json.loads(request.get_data(as_text=True))
    activity = Activity(nickname=post_form['nickname'], creator=post_form['creator'],
                        start_time=datetime.datetime.strptime(post_form['startTime'], '%Y-%m-%d %H:%M:%S'),
                        end_time=datetime.datetime.strptime(post_form['endTime'], '%Y-%m-%d %H:%M:%S'),
                        location=post_form['location'], content=post_form['content'],
                        activity_type=post_form['activityType'], cur_participants_num=post_form['curParticipantsNum'],
                        max_participants_num=post_form['maxParticipantsNum'], a_status=ActivityStatus.enable)
    g.session.add(activity)
    g.session.flush()
    user_activity = UserActivity(user_id=activity.creator, activity_id=activity.id, ua_status=UserActivityStatus.enable)
    g.session.add(user_activity)
    g.session.commit()
    # TODO broadcast
    return jsonify(status="OK", activity_id=activity.id)


@app.route("/activity", methods=["GET"])
@login_required
def get_activity():
    min_index = request.args.get("min_index", 0, type=int)
    max_index = request.args.get("max_index", 0, type=int)
    page_size = request.args.get("page_size", 0, type=int)
    if max_index:
        activities = g.session.query(Activity).filter(
            and_(Activity.id > max_index, Activity.a_status == ActivityStatus.enable)).order_by(
            Activity.id.desc())
    elif min_index and page_size:
        activities = g.session.query(Activity).filter(
            and_(Activity.id < min_index, Activity.a_status == ActivityStatus.enable)).order_by(
            Activity.id.desc()).limit(page_size)
    else:
        activities = g.session.query(Activity).filter(Activity.a_status == ActivityStatus.enable).order_by(
            Activity.id.desc()).limit(page_size)
    return jsonify(status="OK", activities=[Activity.serialize(activity) for activity in activities])


@app.route("/activity/user/<int:user_id>", methods=["GET"])
@login_required
def get_activity_joined(user_id):
    if user_id != current_user.id:
        return jsonify(status="ERROR", message="User id not matched")
    activities = g.session.query(Activity).join(UserActivity, Activity.id == UserActivity.activity_id).filter(
        and_(UserActivity.user_id == user_id, UserActivity.ua_status == UserActivityStatus.enable,
             Activity.a_status == ActivityStatus.enable)).order_by(Activity.id.desc()).all()
    if activities:
        return jsonify(status="OK", activities=[Activity.serialize(activity) for activity in activities])
    else:
        return jsonify(status="ERROR", message="No activity matched")


@app.route("/activity/participants/<int:activity_id>", methods=["GET"])
@login_required
def get_activity_participants(activity_id):
    participants = g.session.query(UserActivity, User).join(User, User.id == UserActivity.user_id).filter(
        and_(UserActivity.activity_id == activity_id, UserActivity.ua_status == UserActivityStatus.enable)).order_by(
        UserActivity.id).all()
    if participants:
        return jsonify(status="OK",
                       participants=[
                           Participant.serialize(Participant(participant['UserActivity'], participant['User']))
                           for participant in participants])
    else:
        return jsonify(status="ERROR")


@app.route("/activity/<int:activity_id>", methods=["PATCH"])
@login_required
def patch_activity(activity_id):
    user_id = request.form.get("user_id", -1, type=int)
    if user_id < 1 or activity_id < 1:
        return jsonify(status="ERROR")
    activity = g.session.query(Activity).get(activity_id)
    if activity.cur_participants_num < activity.max_participants_num:
        activity.cur_participants_num = activity.cur_participants_num + 1
        user_activity = UserActivity(user_id=user_id, activity_id=activity_id, ua_status=UserActivityStatus.enable)
        g.session.flush()
        g.session.add(user_activity)
        g.session.commit()
        return jsonify(status="OK")
    else:
        return jsonify(status="ERROR")


@app.route("/chatterbot/<string:text>", methods=["GET"])
def get_chatterbot(text):
    res = ask_bot(text)
    return jsonify(status="OK", message=res)


if __name__ == "__main__":
    app.config['JSON_AS_ASCII'] = False
    app.config['JSONIFY_MIMETYPE'] = "application/json;charset=utf-8"
    app.run(debug=True)
