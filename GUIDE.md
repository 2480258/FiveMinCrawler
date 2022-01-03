# Customization Guide

We can change the behavior of the crawler by changing JSON and giving its path as a parameter (-p)

**Please read with [Boilerplate](debugWeb.json) for better understandings. You may want to start to customize with this file because copying and pasting this one allows faster customization.**

## Notations

### Document

**Document** means web pages (on other words, HTML files), literally.

### Page

**Page** means kinds of **document**s. For example, You can name **document**s that containing list of images as "
ImageListPage"

**Page**s can convert a **document** to **attribute**s and conditions for **document**s.

### Attribute

**Attribute** means what you want to save. (Parsed text contents, downloaded images, and other things)


## Parser Composition

- Global Condition: Request doesn't match with this regex will not be downloaded. _(essential)_

```json
"bookName": "Debug",
"globalCondition": {
  "uriRegex": "127.0.0.1:30001"
}
```

### Page

- PageName: Literally, name of **page**. Should be unique _(essential)_.
- Condition: Specify whether this **document** is converted to be this page. (using regex) _(essential)_

Note that one **Document** should be attached with one **Page**.

- Internal **Attribute**: See [Internal **Attribute**](#InternalAttribute) section. _(essential)_
- External **Attribute**: See [External **Attribute**](#ExternalAttribute) section. _(essential)_
- Link **Attribute**: See [Link **Attribute**](#LinkAttribute) section. _(essential)_


- Target Container: See [Target Container](#TargetContainer) section. _(essential)_
- Tag: See [Tag](#Tag) section. _(essential)_

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

Internal **Attribute** is **attribute** from downloaded html pages, and can be saved as JSON format.

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global
  scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string to parse. _(essential)_
- Parse mode: selects between OUTER_HTML, TEXT_CONTENT, INNER_HTML. _(essential)_

```json
"internalAttributes": [
  {
    "attributeName": "Heading of Example Text",
    "queryStr": "body > h1",
    "parseMode": "TEXT_CONTENT"
  }
]
```

or allows empty list.

```json
"internalAttributes": []
```

### ExternalAttribute

External **Attribute** is **attribute** that should be downloaded later. (images or other files)

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global
  scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string select range of query. _(optional; default is select all)_
- Uri Regex: Filters URL to downloaded by given regex. _(essential)_


```json
"externalAttributes": [
  {
    "attributeName": "Images of Contents",
    "queryStr": "body > a", // get links only here.
    "uriRegex": "\\/" // get links that matches this regex.
  }
]
```

or allows empty list.

```json
"externalAttributes": []
```

### LinkAttribute

Link **Attribute** is **attribute** that should be requested and parsed further.

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global
  scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string select range of query. _(optional; default is select all)_
- Uri Regex: Filters URL to downloaded by given regex. _(essential)_

```json
"linkAttributes": [
  {
    "attributeName": "Links of Contents",
    "queryStr": "body > a", // get links only here.
    "uriRegex": "\\/" // get links that matches this regex.
  }
]
```

or allows empty list.

```json
"linkAttributes": []
```

### TargetContainer

Target Container specifies how to update downloaded data after crawling.

- If the **page** is marked as **working set** enabled, it means that the page and its children page are _atomic_, they
  will not change over time and no need to be downloaded again.
- If the **page** is marked as **working set** disabled, it will be downloaded again.

```json
"targetContainer": {
    "workingSetMode": "Disabled"
}
```

or

```json
"targetContainer": {
    "workingSetMode": "Enabled"
}
```

If you are not going to use resume feature, just let them Enabled.

### Tag

**Tag** is a kind of **attribute** but extracted from URL.

- Name: Name of **Tag**. Should be unique in **page** scope. _(essential)_
- Tag Regex: Specifies how to extract tag from URL. Only first match of string will be added. _(essential)_
- IsAlias: Specifies whether it is alias of URL. If enabled, **Document**s have same tag will not be downloaded. _(essential)_


```json
"tag": [
  {
    "name": "nameOfTag",
    "tagRegex": "([a-z]+)", // extract tag from this regex via matching with URL
    "isAlias": false
  }
]
```

or allows empty list.
```json
"tag": []
```
## Requester

- User Agent: User agent when sent request. _(essential)_
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

- PageName: Name of **page** to be applied. _(essential)_
- Target Attribute Name: Specifies name of **attribute**s that needed to be exported. _(essential)_
- Adapter: Specifies How to export **attribute**s _(essential)_

```json
"pageName": "Home",
"targetAttributeName": [
    "Images of Contents"
]
```

### ExportAdapter

- Mode: Selects between "Json" and "Binary". If downloaded data is not text-based, can not export as JSON file.
- File Name Tag Expression: Specifies where to export files. We can use [tag](#tag) by writing **&(tagName)**. Some tags are added automatically for sake of usability. See below.

| Tag Name |             Functions             |
|:---------|:---------------------------------:|
| inc      | incremental numbers by attributes |
| ext      |    extension of requested URL.    |
| lastseg  |   last segment of requested URL   |
| name     |         name of attribute         |

Note that directory separators actually work as the directory separator. It means that "a\b" will create b directory under a directory. Also, when filename is duplicated, " - (Dup)" will be appended. But, this behavior doesn't work with files before crawler starts.

So basically not recommended to select duplicated names for to be exported file.

```json
"adapter": {
    "mode": "Binary",
    "fileNameTagExp": "[&(nameOfTag)] - &(lastseg)\\&(inc).&(ext)"
}
```