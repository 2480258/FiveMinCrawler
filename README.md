# Five-Minute Crawler - A Highly Customizable Crawler without Code
_and can be customized in five minutes (maybe.... if you know html and other things XD)_

**This program is very experimental. Stuff will be break.**

## What is this?

**Five-Minute Crawler** is basically a simple crawler, but highly customizable with JSON file.

With this feature, you can make a crawler for specific site very fast.

## Features

- Recursively downloads web pages that pass conditions specified by you (regex)
- Effectively parses downloaded HTML powered by [jsoup](https://github.com/jhy/jsoup)
- Saves parsed and downloaded things (web pages, images, and others) as you want (directory and file names)
- Skips downloading pages if you did before (only applies pages that you want)

## Getting Started

### Command-line Options

    -u                         Starting URL that needed to be crawled
    -p                         Parameter file path
                               (defines custom behavior written by you)
    -r                         (Optional) Resume file (generated by this tool)
                               If you want to avoid download once you did
    -o                         (Optional) Root directory for saving file
                               Default value is where this program saved
    -v                         Use verbose log

### Expected Questions and Answers

- Q: Some of my requests are not performed. 
- A: Request should not be performed if they have same URL (including redirects) or same [tag](GUIDE.md#Tag) which marked alias.


- Q: Some of my contents are not saved.
- A: Parsing is currently performed without JavaScript. Please verify your contents is still visible when turn JavaScript off.


### So, How to Customize This Crawler?
See [Customization Guide](/GUIDE.md).

### Extra Configuration

```json
{
  "MaxRequestThread" : 3
}
```

Currently, that's all.... Can be modified at fivemin.config.json

## Limitations

- No JavaScript support, but planned (with Selenium)

## Used Libraries

> [arrow](https://github.com/arrow-kt/arrow)
>
> [brotli](https://github.com/google/brotli) 
>
>[jsoup](https://github.com/jhy/jsoup)
>
>[kotlin-logging](https://github.com/MicroUtils/kotlin-logging)
>
>[mockk](https://github.com/mockk/mockk)
>
>[okhttp](https://github.com/square/okhttp)

