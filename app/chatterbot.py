import json

from googletrans import Translator
from tencentcloud.common import credential
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.common.profile.http_profile import HttpProfile
from tencentcloud.nlp.v20190408 import nlp_client, models

cred = credential.Credential("AKIDXsxlJaaIkunPlqeAPN4UdW7RUHo55nFQ", "vZx3sQijTv63kX4KJ0QaT4ORDiwtZy6x")
httpProfile = HttpProfile()
httpProfile.endpoint = "nlp.tencentcloudapi.com"

clientProfile = ClientProfile()
clientProfile.httpProfile = httpProfile
client = nlp_client.NlpClient(cred, "ap-guangzhou", clientProfile)
translator = Translator(service_urls=["translate.google.com"])


def ask_bot(query):
    try:

        req = models.ChatBotRequest()
        params = {
            "Query": query
        }
        req.from_json_string(json.dumps(params))

        resp = client.ChatBot(req).Reply
        result = translator.translate(resp)
        return result.text

    except TencentCloudSDKException as err:
        print(err)
        return "I am resting"


if __name__ == "__main__":
    print(ask_bot("Nice to meet you"))
