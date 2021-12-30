# Customization Guide

We can change behavior of crawler by changing JSON and gave its path as parameter (-p)


## Notations

### Document

**Document** means web pages (on other words, HTML files), literally.

### Page

**Page** means kinds of **document**s. For example, You can name **document**s contains list of images as "ImageListPage"

**Page**s can convert a **document** to **attribute**s and conditions for **document**s.


### Attribute

**Attribute** means what you want to save. (Parsed text contents, downloaded images, and other things)

## Parser Customization

### Page

- PageName: Literally, name of **page**. Should be unique _(essential)_.
- Condition: Specify whether this **document** is converted to be this page. (using regex) _(essential)_
  

- Internal **Attribute**: See Internal **Attribute** section. _(essential)_
- External **Attribute**: See External **Attribute** section. _(essential)_
- Link **Attribute**: See Link **Attribute** section. _(essential)_


- Target Container: See Target Container section. _(essential)_
- Tag: See Tag section. _(essential)_


- Target Requester Engine: See Requester section. _(essential)_

### Internal Attribute

Internal **Attribute** is **attribute** from downloaded html pages, and can be saved as JSON format.

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string to parse. _(essential)_
- Parse mode: can select between OUTER_HTML, TEXT_CONTENT, INNER_HTML. _(essential)_

### External Attribute

External **Attribute** is **attribute** that should be downloaded later. (images or other files)

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string select range of query. _(optional; default is select all)_
- Uri Regex: Filter URL to downloaded by given regex. _(essential)_

### Link Attribute

Link **Attribute** is **attribute** that should be requested and parsed further.

- **Attribute** Name: Name of **attribute**. Should be unique in **page** scope and recommend being unique in global scope due to possible file duplication. _(essential)_
- Query String: CSS selector or JSoup query string select range of query. _(optional; default is select all)_
- Uri Regex: Filter URL to downloaded by given regex. _(essential)_

### Target Container (Working Set)

Target Container is to specify how to update downloaded data after crawled.

- If the **page** is marked as **working set** enabled, it means that the page and its children page are _atomic_, they will not change overtime and no need to be downloaded again.
- If the **page** is marked as **working set** disabled, it will be downloaded again.

### Tag

**Tag** is a kind of **attirbute** but extracted from URL.

- Name: Name of **Tag**. Should be unique in **page** scope.
- Tag Regex: Specify how to extract tag from URL. Only first match of string will be added.
- IsAlias: Specify whether it is alias of URL. If enabled, **Document**s have same tag will not be downloaded.

## Requester

- User Agent: User agent when sent request.

## Export

- PageName: Name of **page** to be applied.
- Target Attribute Name: Specify name of **attribute**s that needed to be exported.
- Adapter: Specify How to export **attribute**s

### Export Adapter

- Mode: Select between "Json" and "Binary". If downloaded data is not a text based, can not export as JSON file.
- File Name Tag Expression: Specify where to export files.
- > basically &(Tag Name) replaced to tag name, and '\' means directory path.
  >  
  > Note that there are few special tags: 
  > 
  > &(inc): the index of attributes of multiple elements.
  > 
  > &(ext): Extension of downloaded URL (if provided). useful if attribute is images or other things.
  > 
  > &(lastseg): Last segment of downloaded URL (if provided)
  > 
  > &(name): Name of attributes.