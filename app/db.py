import datetime

from sqlalchemy import Column, String, Integer, DateTime, create_engine, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from flask_login import UserMixin

Base = declarative_base()


class User(Base, UserMixin):
    __tablename__ = 'user'

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(20), nullable=False, index=True, unique=True)
    nickname = Column(String(20), nullable=False)
    avatar = Column(String(255), nullable=False)
    location = Column(String(20), nullable=False)
    birth = Column(String(8), nullable=False)
    sex = Column(String(1), nullable=False)
    pw = Column(String(64), nullable=False)
    signature = Column(String(20), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    def serialize(self):
        return {
            'id': self.id,
            'username': self.username,
            'nickname': self.nickname,
            'avatar': self.avatar,
            'location': self.location,
            'birth': self.birth,
            'sex': self.sex,
            'signature': self.signature
        }


class Group(Base):
    __tablename__ = 'group'

    id = Column(Integer, primary_key=True, autoincrement=True)
    nickname = Column(String(20), nullable=False)
    avatar = Column(String(255), nullable=False)
    member_number = Column(Integer, nullable=False)
    announcement = Column(String(200), nullable=True)
    creator = Column(Integer, nullable=False)
    administrator = Column(Integer, nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    def serialize(self):
        return {
            'id': self.id,
            'nickname': self.nickname,
            'avatar': self.avatar,
            'memberNumber': self.member_number,
            'announcement': self.announcement,
            'creator': self.creator,
            'administrator': self.administrator
        }


class Friend(Base):
    __tablename__ = 'friend'

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    friend_id = Column(Integer, ForeignKey("user.id"), nullable=False)
    nickname = Column(String(20), nullable=True)
    f_status = Column(String(10), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    friend = relationship("User")

    def serialize(self):
        return {
            'id': self.id,
            'userId': self.user_id,
            'friendId': self.friend_id,
            'nickname': self.nickname,
            'friend': User.serialize(self.friend)
        }

    def serialize_all_str(self):
        return str(Friend.serialize(self))


class UserGroup(Base):
    __tablename__ = 'user_group'

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("User.id"), nullable=False, index=True)
    group_id = Column(Integer, ForeignKey("group.id"), nullable=False, index=True)
    nickname = Column(String(20), nullable=True)
    ug_status = Column(String(10), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    group = relationship("Group")

    def serialize(self):
        return {
            'id': self.id,
            'userId': self.user_id,
            'groupId': self.group_id,
            'nickname': self.nickname,
            'group': Group.serialize(self.group)
        }


class Message(Base):
    __tablename__ = 'message'

    id = Column(Integer, primary_key=True, autoincrement=True)
    from_id = Column(Integer, nullable=False)
    dest_id = Column(Integer, nullable=False)
    content = Column(String(255), nullable=False)
    content_type = Column(String(10), nullable=False)
    msg_type = Column(String(10), nullable=False)
    m_status = Column(String(10), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    def serialize(self):
        return {
            'id': self.id,
            'fromId': self.from_id,
            'destId': self.dest_id,
            'content': self.content,
            'contentType': self.content_type,
            'msgType': self.msg_type,
            'status': self.m_status,
            'createDate': self.create_date.strftime('%Y%m%d %H:%M:%S')
        }


class Activity(Base):
    __tablename__ = 'activity'

    id = Column(Integer, primary_key=True, autoincrement=True)
    nickname = Column(String(30), nullable=False)
    creator = Column(Integer, nullable=False)
    start_time = Column(DateTime, nullable=False)
    end_time = Column(DateTime, nullable=False)
    location = Column(String(20), nullable=False)
    content = Column(String(255), nullable=False)
    activity_type = Column(String(10), nullable=False)
    cur_participants_num = Column(Integer, nullable=False)
    max_participants_num = Column(Integer, nullable=False)
    a_status = Column(String(10), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    def serialize(self):
        return {
            'id': self.id,
            'nickname': self.nickname,
            'creator': self.creator,
            'startTime': self.start_time.strftime('%Y-%m-%d %H:%M:%S'),
            'endTime': self.end_time.strftime('%Y-%m-%d %H:%M:%S'),
            'location': self.location,
            'content': self.content,
            'activityType': self.activity_type,
            'curParticipantsNum': self.cur_participants_num,
            'maxParticipantsNum': self.max_participants_num
        }


class UserActivity(Base):
    __tablename__ = 'user_activity'

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    activity_id = Column(Integer, nullable=False, index=True)
    ua_status = Column(String(10), nullable=False)
    create_date = Column(DateTime, nullable=False, default=datetime.datetime.now)
    update_date = Column(DateTime, nullable=False, default=datetime.datetime.now, onupdate=datetime.datetime.now)

    def serialize(self):
        return {
            'id': self.id,
            'userId': self.user_id,
            'activityId': self.activity_id
        }


class FCMToken(Base):
    __tablename__ = 'fcm_token'

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, unique=True, index=True)
    token = Column(String(256), nullable=False)


def my_database():
    engine = create_engine('mysql+mysqlconnector://dbuser:password@localhost:3306/jc')
    db_session = sessionmaker(bind=engine)
    return db_session()
