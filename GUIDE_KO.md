# 커스터마이징 가이드

JSON 파일을 수정하고 이를 파라미터 경로 (-p) 로 지정함에 따라 크롤러의 동작을 변경할 수 있습니다.

**만약 질문이 있다면 [이슈](https://github.com/2480258/FiveMinCrawler/issues) 탭에 자유롭게 작성해주세요.**

## 용어

### Document

**Document** 는 웹 페이지 (HTML)을 의미합니다.

### Page

**Page** 는 **document** 의 종류를 의미합니다. 예를 들어 이미지 리스트를 포함한 **document**는 "ImageListPage"로 지정될 수 있습니다.

**Page**는 **document**의 조건을 평가해서 **attribute**로 변환할 수 있습니다.

### Attribute

**Attribute** 는 저장되기를 원하는 데이터입니다. (파싱된 텍스트, 이미지 등)

## 파서 구성

- 광역 조건: 해당 정규식에 URL이 해당되지 않는 요청은 거부됩니다. _(필수)_

```json
"bookName": "Debug",
"globalCondition": {
  "uriRegex": "127.0.0.1:30001"
}
```

### Page

- PageName: **page**의 이름입니다. 유일해야 합니다. _(필수)_.
- Condition: 이 **document** 이 **page**로 변환될 수 있는지를 지정합니다. (정규식 사용) _(필수)_

하나의 **Document** 는 반드시 하나의 **Page**로 지정되어야 함을 유념하십시오.

- Internal **Attribute**: [Internal **Attribute**](#InternalAttribute) 섹션을 보세요. _(필수)_
- External **Attribute**: [External **Attribute**](#ExternalAttribute) 섹션을 보세요. _(필수)_
- Link **Attribute**: [Link **Attribute**](#LinkAttribute) 섹션을 보세요. _(필수)_


- Target Container: [Target Container](#TargetContainer) 섹션을 보세요. _(essential)_
- Tag: [Tag](#Tag) 섹션을 보세요. _(필수)_

```json
"pages": [
  {
    "pageName": "Home",
    "condition": {
      "uriRegex": "127.0.0.1:30001/home"
    },
    "internalAttributes": [
      See Internal Attribute section
    ],
    "linkAttributes": [
      See Link Attribute section
    ],
    "externalAttributes": [
      See External Attribute section
    ],
    "targetContainer": {
      See Target Container section
    },
    "tag": [
      See Tag section
    ],
    "targetRequesterEngine": {
      "targetRequester": "Default" // just let this default
    }
  }
]
```

### InternalAttribute

Internal **Attribute** 는 다운로드 된 HTML 페이지에서 얻어진 **attribute** 이고, JSON 파일로 저장될 수 있습니다.

- **Attribute** Name: **attribute**의 이름입니다. **page** 범위 내에서 유일해야 하고, 파일의 중복 때문에 전체 범위에서 유일하게 지정하는 것이 좋습니다. _(필수)_
- Query String: 파싱을 하기 위한 CSS Selector 혹은 Jsoup와 호환되는 문자열입니다. _(필수)_
- Parse mode: OUTER_HTML, TEXT_CONTENT, INNER_HTML 중에 선택해야 합니다. _(필수)_

```json
"internalAttributes": [
  {
    "attributeName": "Heading of Example Text",
    "queryStr": "body > h1",
    "parseMode": "TEXT_CONTENT"
  }
]
```

혹은 빈 리스트를 허용합니다.

```json
"internalAttributes": []
```

### ExternalAttribute

External **Attribute**는 나중에 다운로드되어야 할 **attribute** 를 나타냅니다. (이미지 등의 다른 파일)

- **Attribute** Name: **attribute**의 이름입니다. **page** 범위 내에서 유일해야 하고, 가능한 한 중복을 피하기 위해 전역에서 유일하게 지정하는 것이 좋습니다. _(필수)_
- Query String: 링크를 추출하기 위한 범위를 지정하는 CSS selector 혹은 JSoup 쿼리 문자열입니다. _(옵션; 기본값은 모두 선택입니다.)_
- Uri Regex: URL을 필터링하기 위한 정규식입니다. _(필수)_

```json
"externalAttributes": [
  {
    "attributeName": "Images of Contents",
    "queryStr": "body > a", // get links only here.
    "uriRegex": "\\/" // get links that matches this regex.
  }
]
```

혹은 빈 리스트를 허용합니다.

```json
"externalAttributes": []
```

### LinkAttribute

Link **Attribute**: 요청되고 파싱되어야 하는 **attribute**를 나타냅니다.

- **Attribute** Name: **attribute**의 이름입니다. **page** 범위에서 유일해야 하고, 가능한 한 중복을 피하기 위해 전역에서 유일한 것이 좋습니다. _(필수)_
- Query String: 링크를 추출하기 위한 범위를 지정하는 CSS selector 혹은 JSoup 쿼리 문자열입니다. _(옵션; 기본값은 모두 선택입니다.)_
- Uri Regex: URL을 필터링하기 위한 정규식입니다. _(필수)_
- Dest Page: 특정 **Page**를 파싱 결과로 고정합니다. 특정 **Page**의 정규식을 얻기 힘들 때 유용합니다.

```json
"linkAttributes": [
  {
    "attributeName": "Links of Contents",
    "queryStr": "body > a", // get links only here.
    "uriRegex": "\\/", // get links that matches this regex.
    "destPage": "entry" // (Optional)
  }
]
```

혹은 빈 리스트를 허용합니다.

```json
"linkAttributes": []
```

### TargetContainer

Target Container는 크롤링 후 어떻게 데이터를 업데이트 할 것인지를 지정합니다.

- 만약 **page**가 **working set** 활성화로 표시되어 있으면, 이 페이지와 그 자식 페이지가 _원자적_ 이며, 시간에 따라 변하지 않고 그러므로 다시 다운로드 될 필요가 없음을 의미합니다.
- 만약 **page**가 **working set** 비활성화로 표시되어 있으면, 다시 다운로드 될 수 있음을 의미합니다.

```json
"targetContainer": {
  "workingSetMode": "Disabled"
}
```

또는

```json
"targetContainer": {
  "workingSetMode": "Enabled"
}
```

만약 재개 파일을 사용할 예정이 없다면, 그냥 활성화로 두십시오.

### Tag

**Tag**는 **attribute**의 한 종류지만 URL로부터 추출됩니다.

- Name: **Tag**의 이름입니다. **page** 범위에서 유일해야 합니다. _(필수)_
- Tag Regex: 어떻게 URL이 추출되는 지를 결정합니다. 오직 첫 번째 매치만이 Tag로 등록됩니다. _(필수)_
- IsAlias: 이 **Tag**가 URL의 별칭인지를 표시합니다. 활성화될 경우, 같은 Tag를 가진 **Document**는 다운로드되지 않을 것입니다. _(필수)_

```json
"tag": [
  {
    "name": "nameOfTag",
    "tagRegex": "([a-z]+)", // extract tag from this regex via matching with URL
    "isAlias": false
  }
]
```

혹은 빈 리스트를 허용합니다.

```json
"tag": []
```

## Requester

- User Agent: 요청 시 사용할 User agent 입니다. _(필수)_

```json
"requestFormat": {
  "engines": [
    {
      "requesterEngineName": "Default", // just let them "Default".
      "type": "Default", // just let them "Default".
      "requesters": [
        {
          "userAgent": "I am User agent!"
        }
      ]
    }
  ],
  "cookiePolicies": []  // just let them empty.
}
```

## Export

### ExportPage

- PageName: 적용될 **page**의 이름입니다. _(필수)_
- Target Attribute Name: 출력이 필요한 해당 **page**의 **attribute**의 이름을 표시합니다. _(필수)_
- Adapter: 어떻게 **attribute**를 출력할지를 결정합니다. _(필수)_

```json
"pageName": "Home",
"targetAttributeName": [
  "Images of Contents"
]
```

### ExportAdapter

- Mode: "Json"와 "Binary" 중 선택합니다. 만약 다운로드된 데이터가 텍스트 기반이 아닐 경우, JSON 파일로 출력할 수 없습니다.
- File Name Tag Expression: 어디에 파일을 출력할 지 결정합니다. **&(tagName)** 으로 작성하여 [tag](#tag)의 값을 사용할 수 있습니다. 몇몇 태그가 편의성을 위해서
  추가됩니다. 아래를 참고하십시오.

| 태그 이름   |         기능         |
|:--------|:------------------:|
| inc     | attribute마다 증가하는 값 |
| ext     |      URL의 확장자      |
| lastseg |  URL의 마지막 segment  |
| name    |   attribute의 이름    |

폴더 구분자가 그대로 동작한다는 점에 대해 유의하십시오. 예를 들어 "a\b" 문자열은 a 폴더 아래 b 폴더를 생성함을 의미합니다. 또한, 파일 이름이 중복되었을 때는, " - (Dup)" 이 이름에 추가될 것입니다. 하지만 이러한 동작은 크롤러가 시작하기 전에 있었던 파일에 대해서는 적용되지 않습니다.

그러므로 기본적으로 출력될 파일에 중복된 이름을 적용하는 것은 권장되지 않습니다.

```json
"adapter": {
  "mode": "Binary",
  "fileNameTagExp": "[&(nameOfTag)] - &(lastseg)\\&(inc).&(ext)"
}
```