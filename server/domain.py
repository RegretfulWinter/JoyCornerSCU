from constant import MessageType
from db import UserActivity, User


def create_by_friend(friend, user, message):
    return Communication(friend.friend_id, MessageType.p2p, user.nickname, friend.nickname, user.avatar,
                         message.content, message.create_date)


def create_by_group(group, message):
    return Communication(-1 * group.id, MessageType.group, group.nickname, group.nickname, group.avatar,
                         message.content,
                         message.create_date)


class Communication:
    def __init__(self, contact_id, contact_type, nickname, remark, avatar, latest_message, when):
        self.contact_id = contact_id
        self.contact_type = contact_type
        self.nickname = nickname
        self.remark = remark
        self.avatar = avatar
        self.latest_message = latest_message
        self.when = when

    def serialize(self):
        return {
            'contactId': self.contact_id,
            'contactType': self.contact_type,
            'nickname': self.nickname,
            'remark': self.remark,
            'avatar': self.avatar,
            'latestMessage': self.latest_message,
            'when': self.when.strftime('%Y%m%d %H:%M:%S')
        }


class Participant:
    def __init__(self, ussr_activity, user):
        self.user_id = ussr_activity.user_id
        self.activity_id = ussr_activity.activity_id
        self.username = user.username
        self.nickname = user.nickname
        self.avatar = user.avatar

    def serialize(self):
        return {
            'userId': self.user_id,
            'activityId': self.activity_id,
            'username': self.username,
            'nickname': self.nickname,
            'avatar': self.avatar
        }
