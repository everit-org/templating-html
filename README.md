templating-web
==============

## Introduction

Everit Web Templating can be used to render web pages based on HTML
templates. The library can also be used to generate XMLs as XML syntax
is compatible with HTML.

## Attributes

### bookmark

### each

### var

### render

all, body, tag, none; e.g.: ewt-render="(ewt_directAccess) ? 'body' : 'none'"

###text

Alternative content for the tag

### utext

Unescaped alternative content for the tag.

### attr

Map, attribute values

### attr-*

Value of an attribute

### attrprepend

### attrprepend-*

### attrappend

### attrappend-*

### parsebody: default false

## Usage

## Specialties

### Works with XML

Web Templating can be used to generate XML documents as XML has stricter
rules than HTML.

    // TODO example of using custom namespace.

### Works with unclosed elements

In XMLs all tags must be closed. However, in HTML the same rules are not
applied, it is possible to use a tag without closing it. E.g.:

    <div><span>MyText</div>

### Extremely fast

7-10x faster than concurrent templating engines (e.g.: Thymeleaf).
