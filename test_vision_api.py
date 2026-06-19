import requests
import base64
import sys
import json
from pathlib import Path

API_KEY = "YOUR_API_KEY"
ENDPOINT = "https://ark.cn-beijing.volces.com/api/v3"

# 两个确认存在的模型
# MODEL = "doubao-1.5-vision-pro-250328"
MODEL = "doubao-1-5-vision-pro-32k-250115"

if len(sys.argv) < 2:
    print("Usage: python test_vision_api.py <image_path> [api_key]")
    sys.exit(1)

image_path = sys.argv[1]
if len(sys.argv) >= 3:
    API_KEY = sys.argv[2]

if not Path(image_path).exists():
    print(f"File not found: {image_path}")
    sys.exit(1)

with open(image_path, "rb") as f:
    image_data = f.read()

base64_image = base64.b64encode(image_data).decode("utf-8")

payload = {
    "model": MODEL,
    "messages": [
        {
            "role": "user",
            "content": [
                {
                    "type": "image_url",
                    "image_url": {
                        "url": f"data:image/jpeg;base64,{base64_image}"
                    }
                },
                {
                    "type": "text",
                    "text": """请分析图片中的衣物，严格以纯JSON格式返回以下字段，不要包含任何其他文字或markdown标记：
{
  "name": "衣物名称",
  "category": "上衣、下装、外套、连衣裙、鞋、配饰",
  "color": "白色、黑色、红色、蓝色、绿色、黄色、紫色、粉色、灰色、棕色、橙色、其他",
  "season": "春秋、夏、冬",
  "style": "休闲、商务、运动、正式",
  "description": "简要描述"
}

注意：
1. 只返回JSON，不要有其他内容
2. 如果图片中没有衣物，category填"配饰"并在description中说明"""
                }
            ]
        }
    ],
    "temperature": 0.2,
    "max_tokens": 1024
}

url = f"{ENDPOINT.rstrip('/')}/chat/completions"
headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

print(f"POST {url}")
print(f"Model: {MODEL}")
print(f"Image: {image_path} ({len(image_data)} bytes, base64: {len(base64_image)} chars)")
print()

resp = requests.post(url, json=payload, headers=headers, timeout=60)
print(f"HTTP {resp.status_code}")
print()

try:
    data = resp.json()
    print(json.dumps(data, indent=2, ensure_ascii=False))
except Exception:
    print(resp.text)
