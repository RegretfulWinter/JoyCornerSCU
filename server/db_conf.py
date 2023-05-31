import hashlib

from sqlalchemy import MetaData, Table

from db import *


def create_tables():
    engine = create_engine('mysql+mysqlconnector://dbuser:password@localhost:3306/jc')
    meta = MetaData()
    user = Table(
        'user', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('username', String(20), nullable=False, index=True, unique=True),
        Column('nickname', String(20), nullable=False),
        Column('avatar', String(255), nullable=False),
        Column('location', String(20), nullable=False),
        Column('birth', String(8), nullable=False),
        Column('sex', String(1), nullable=False),
        Column('pw', String(64), nullable=False),
        Column('signature', String(20), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    group = Table(
        'group', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('nickname', String(20), nullable=False),
        Column('avatar', String(255), nullable=False),
        Column('member_number', Integer, nullable=False),
        Column('announcement', String(200), nullable=True),
        Column('creator', Integer, nullable=False),
        Column('administrator', Integer, nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    friend = Table(
        'friend', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('user_id', Integer, nullable=False, index=True),
        Column('friend_id', Integer, nullable=False),
        Column('nickname', String(20), nullable=True),
        Column('f_status', String(10), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    user_group = Table(
        'user_group', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('user_id', Integer, nullable=False, index=True),
        Column('group_id', Integer, nullable=False, index=True),
        Column('nickname', String(20), nullable=True),
        Column('ug_status', String(10), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    message = Table(
        'message', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('from_id', Integer, nullable=False),
        Column('dest_id', Integer, nullable=False),
        Column('content', String(255), nullable=False),
        Column('content_type', String(10), nullable=False),
        Column('msg_type', String(10), nullable=False),
        Column('m_status', String(10), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    activity = Table(
        'activity', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('nickname', String(20), nullable=False),
        Column('creator', Integer, nullable=False),
        Column('start_time', DateTime, nullable=False),
        Column('end_time', DateTime, nullable=False),
        Column('location', String(20), nullable=False),
        Column('content', String(255), nullable=False),
        Column('activity_type', String(10), nullable=False),
        Column('cur_participants_num', Integer, nullable=False),
        Column('max_participants_num', Integer, nullable=False),
        Column('a_status', String(10), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )

    user_activity = Table(
        'user_activity', meta,
        Column('id', Integer, primary_key=True, autoincrement=True),
        Column('user_id', Integer, nullable=False, index=True),
        Column('activity_id', Integer, nullable=False, index=True),
        Column('ua_status', String(10), nullable=False),
        Column('create_date', DateTime, nullable=False, default=datetime.datetime.now),
        Column('update_date', DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    )
    meta.create_all(engine)


def get_pw(pw):
    return hashlib.sha256(pw.encode()).hexdigest()


def create_user():
    session = my_database()
    # user = User(username='1155166603', nickname='louis liu', avatar='/default.jpg', location='CUHK', birth='19960123',
    #             sex='1', pw=get_pw('password'), signature='nothing to say')
    user = User(username='1155107871', nickname='hermes', avatar='/default.jpg', location='CUHK', birth='19990101',
                sex='1', pw=get_pw('password'), signature='nothing to say')
    session.add(user)
    session.commit()
    session.close()


def update_user():
    session = my_database()
    user = session.query(User).get(1)
    user.pw = get_pw('password')
    session.commit()
    session.close()


def create_group():
    session = my_database()
    group = Group(nickname='Werewolf in CUHK', avatar='/default.jpg', member_number=1,
                  announcement='Welcome and best regards',
                  creator=1, administrator=1)
    session.add(group)

    user_group = UserGroup(user_id=1, group_id=1, ug_status='ENABLE')
    session.add(user_group)

    session.commit()
    session.close()


def create_friend():
    friend1 = Friend(user_id=1, friend_id=2, nickname='付鹤鸣', f_status='ENABLE')
    friend2 = Friend(user_id=2, friend_id=1, nickname='刘耀晖', f_status='ENABLE')
    session = my_database()
    session.add(friend1)
    session.add(friend2)
    session.commit()
    session.close()


def create_message():
    message1 = Message(from_id=1, dest_id=2, content='Nice to meet you', content_type='TEXT', msg_type='CHAT',
                       m_status='READ')
    message2 = Message(from_id=2, dest_id=1, content='Me too', content_type='TEXT', msg_type='CHAT',
                       m_status='READ')
    session = my_database()
    session.add(message1)
    session.add(message2)
    session.commit()
    session.close()


def create_activity():
    session = my_database()
    start_time = datetime.datetime.strptime('20211121 19:00:00', '%Y%m%d %H:%M:%S')
    end_time = datetime.datetime.strptime('20211121 22:00:00', '%Y%m%d %H:%M:%S')
    activity = Activity(nickname='Let\'s play Werewolf', creator='1', start_time=start_time,
                        end_time=end_time, location='YIA iLounge in CUHK',
                        content='Wonderful werewolf activity, looking forward to your joining',
                        activity_type='Werewolf', cur_participants_num=1, max_participants_num=12, a_status='ENABLE')
    session.add(activity)

    user_activity = UserActivity(user_id=1, activity_id=1, ua_status='ENABLE')
    session.add(user_activity)

    session.commit()
    session.close()
