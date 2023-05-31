import firebase_admin
from firebase_admin import credentials, messaging

from app.constant import MessageContentType, MessageType, ActionType
from app.db import Friend

cred = credentials.Certificate(
    "/Users/liuyaohui/Documents/IEMS5722 Mobile Network Programming and Distributed Server Architecture/final "
    "project/joycorner-iems-firebase-adminsdk-f5hpl-8667b1791e.json")
firebase_admin.initialize_app(cred)


def new_message_notification(from_id, nickname, content, timestamp, content_type, msg_type, to_token):
    display_msg = content
    if content_type == MessageContentType.image:
        display_msg = '[Picture]'
    elif content_type == MessageContentType.video:
        display_msg = '[Video]'
    elif content_type == MessageContentType.audio:
        display_msg = '[Audio]'

    message = messaging.Message(
        data={
            'from_id': str(from_id),
            'nickname': nickname,
            'content': content,
            'timestamp': timestamp,
            'content_type': content_type,
            'msg_type': msg_type,
            'display_msg': display_msg
        },
        token=to_token,
    )
    response = messaging.send(message)


def new_friend_application_notification(from_id, nickname, to_token):
    message = messaging.Message(
        data={
            'from_id': str(from_id),
            'nickname': nickname,
            'msg_type': MessageType.action,
            'action_type': ActionType.apply_friend
        },
        token=to_token,
    )
    response = messaging.send(message)


def confirm_friend_notification(friend, to_token):
    message = messaging.Message(
        data={
            'friend': Friend.serialize_all_str(friend),
            'msg_type': MessageType.action,
            'action_type': ActionType.confirm_friend
        },
        token=to_token,
    )
    response = messaging.send(message)
