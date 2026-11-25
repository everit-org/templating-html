# Templating HTML

## Introduction

Everit Web Templating can be used to render web pages based on HTML templates. The library can also be used to generate XMLs as XML syntax is compatible with HTML.

The goals of the templating engine are:

- Be extremely fast
- Have a very small binary and memory footprint
- Do not do more just HTML templating (No MVC, URL resolving, etc.)
- The output is as close to the original template as possible. All of the linebreaks and whitespaces are left there. Bugfixing of a template is much easier in this way. In case you want to save bandwidth, use a ZIP stream. Three whitespaces will cost the almost the same as one.
- Allow the programmers to use the code created by web designers as it is delivered. Only decoration should with templating specific attributes should be done, so the web designer can continue working on the code.

---

## Usage

```java
// Get a template from somewhere (e.g.: from a file)
String text = "<span data-eht-text='name' />";

// Instantiate any expression compiler (e.g.: MVEL)
ExpressionCompiler expressionCompiler = new MvelExpressionCompiler();

// Instantiate template compiler and pass the expression compiler
HTMLTemplateCompiler compiler = new HTMLTemplateCompiler(expressionCompiler);

// Get a classloader that can be used to compile the template if necessary
ClassLoader cl = this.getClass().getClassLoader();

// Instantiate a ParserConfiguration
ParserConfiguration configuration = new ParserConfiguration(cl);

// Compile the template
CompiledTemplate template = compiler.compile(text, configuration);

/////////////////////////////////////////////////////////////////////////
// Rendering the template
/////////////////////////////////////////////////////////////////////////

// Create a map for the variables that can be used within the template
Map<String, Object> vars = new HashMap<>();
vars.put("name", "John Doe");

// Get a Writer from somewhere (e.g.: a ResponseWriter or a StringWriter
Writer writer = new StringWriter();

// Render the template to the writer
template.render(writer, vars);

// In this example we write out the result to the standard output
System.out.println(writer.toString());
```

---

## Attributes

### foreach

Iterates through the elements of an Iterable element, Object array or primitive array. The value of the attribute must be a Map where the key is the name of the variable that holds the element of the iteration, the value holds the collection that is iterated. Example based on MVEL:

```html
<table>
  <tr data-eht-foreach="['user' : users]">
    <td data-eht-text="user.firstName">John</td>
    <td data-eht-text="user.lastName">Doe</td>
  </tr>
</table>
```

An array of two Strings can be defined as the key of the Map. In that case the first element of the array will show the name of the variable of the element, while the second String will be the name of the index variable. Example based on MVEL expressions:

```html
<table>
  <tr data-eht-foreach="[({'user', 'index'}) : users]">
    <td data-eht-text="index + 1">1</td>
    <td data-eht-text="user.firstName">John</td>
    <td data-eht-text="user.lastName">Doe</td>
  </tr>
</table>
```

Multi-iteration is also supported when the Map has more entries. Use this only if you are sure that the Map keeps its insertion order, otherwise the output might be "randomized".

### code

It is possible to run a code block. In most cases it is necessary to declare new variables.

```html
<p data-eht-code="var myVar1 = 'Hello'; var myVar2 = myVar1 + ' world!'"
   data-eht-text="myVar2" />
```

The code snippet above will write out

```html
 <p>Hello world!</p>
```

### var

Sometimes it is not possible to declare variables with the expression language that are put into the variables map. That is where *var* helps.

With *var* it is possible to declare / assigne one or more variables. The value of the attribute must be a Map, where the keys of the Map are the name of the variables and the values of the Map are the values of the variables. Example based on MVEL:

```html
<div data-eht-var="['firstName' : user.firstName,
                    'lastName' : user.lastName]">
    ...
</div>
```

### render

A case-insensitive String value that tells which parts of the element should be rendered:

- **all**: The element and its body is rendered. This is the default.
- **content**: Only the body of the element is rendered, the element itself will not be part of the output
- **tag**: Only the element itself is rendered, its content is not
- **none**: The element is not rendered at all

Boolean values can also be used in the following way:

- **true**: The same as all
- **false**: The same as none

The value of the render attribute can be defined dynamically:

```html
<span data-eht-render="user.firstName == 'John' ? 'all' : 'none'">...</span>
```

In case none is specified as a constant, the parsing of the element and its content is skipped completely. This is useful if we want to comment something out from the template in the way that it is not processed:

```html
<span data-eht-render="'none'">...</span>
```

### text

Replaces the content of the element with the result of the evaluation of the value of the attribute. The text will be escaped.

```html
<td data-eht-text="user.firstName">John</td>
```

This attribute should be used also if the content of the tag is written with a function call directly to the writer. E.g.:

```html
<td data-eht-text="appender.writeOnWriter()">John</td>
```

In the example above, the writeOnWriter might be a void function that writes onto the same writer that is passed to the CompiledTemplate.render(...) function.

### utext

Same as text, but the text will not be escaped.

### attr

A Map of attribute overrides.

```html
<div id="testId" data-eht-attr="['id' : 'runtimeId', 'class' : 'someClass']" />
```

The output of the template above will be the following:

```html
<div id="runtimeId" class="someClass" />
```

<a name="attr_"></a>

### attr-\*

It possible to override the value of any attribute by defining a new value.

```html
<span id="testId" data-eht-attr-id="'runtimeId'" />
```

### attrprepend

Prepends the value of one or more attributes. A Map should be provided where the keys are the name of the attributes and the values of the Map are the new values.

```html
<div class="class2"
  data-eht-attrprepend="['runtimeId' : 'runtime',
                         'class' : 'class1 ']" />
```

The output of the template above will be the following:

```html
<div class="class1 class2" id="runtimeId" />
```

<a name="attrprepend_"></a>

### attrprepend-\*

As with *attr-\**, it is possible to prepend attributes. A String should be provided!

```html
<div id="Id" data-eht-attrprepend-id="'runtime'" />
```

The output of the template above will be the following:

```html
<div id="runtimeId" />
```

### attrappend

Same as *attrPrepend*, but it appends the newly defined values to the end of the original attributes.

<a name="attrappend_"></a>

### attrappend-\*

Same as *attrprepend-\**, but it appends the newly defined value to the end of the original attribute.

### inline

It is possible to mix the syntax of the template with other template syntaxes. This is useful if some other format should be embedded whithin a template, e.g.: a javascript.

Inline template compilers must be defined during the initialization of the HTML template compiler.

```java
// Instantiate any expression compiler (e.g.: MVEL)
ExpressionCompiler expressionCompiler = new MvelExpressionCompiler();

// Create the inline compilers and put them into a Map
TemplateCompiler textCompiler = new TextTemplateCompiler(expressionCompiler);

Map<String, TemplateCompiler> inlineCompilers = new HashMap<>();
inlineCompilers.put("text", textCompiler);

// Instantiate template compiler and pass the expression compiler
HTMLTemplateCompiler compiler = new HTMLTemplateCompiler(expressionCompiler);

// Compile and render the template...
```

In the example above one inline compiler was defined with the text identifier. In the template the same identifier should be used within the *fragment* attribute as a constant String.

```html
<script type="text/javascript" data-eht-inline="'text'">
  var firstName = @{user.firstName};
</script>
```

### fragment

It is possible to define fragments that can be rendered separately, too.

```html
<div data-eht-fragment="'myFragment'">
  This is the content of the fragment
</div>
```

The fragment can be rendered programmatically:

```java
template.render(writer, vars, "myFragment");
```

It is also possible to render a fragment from any place of the template that defined it:

```html
<div data-eht-utext="template_ctx.renderFragment('myFragment')" />
```

Let's say that a fragment should be rendered only if it is called:

```html
<div data-eht-fragment="'myFragment'"
     data-eht-render="template_ctx.fragmentId == 'myFragment'">
  This is the content of the fragment
</div>
```

<a name="replacing_the_attribute_prefix"></a>

### Replacing the attribute prefix

By default data-eht- is the prefix for the templating attributes. This is good for HTML as in HTML any attribute starting with *data-* are allowed.

However, in XML someone might wants to use an XML namespace instead. The default prefix can be overridde in the constructor of HTMLTemplateCompiler.

```java
new HTMLTemplateCompiler(expressionCompiler, inlineCompilers, "eht:");
```

In the XML, the namespace can be defined with the prefix above. In case the namespace definition should be hidden in the output, it can be deleted by overriding the value of the namespace declaration to null with the *attr* attribute.

```xml
<myElement xmlns:eht="http://ehtNamespace"
           eht:attr="['xmlns:eht' : null]">
   ....
</myElement>
```

---

## Specialties

<a name="works_with_xml"></a>

### Works with XML

Web Templating can be used to generate XML documents as XML has stricter rules than HTML.

```java
// TODO example of using custom namespace.
```

<a name="works_with_unclosed_elements"></a>

### Works with unclosed elements

In XML all tags must be closed. However, in HTML the same rules are not applied, it is possible to use a tag without closing it. E.g.:

```html
<div><span>MyText</div>
```

<a name="extremely_fast"></a>

### Extremely fast

Everit HTML templating is often 7-10x faster than concurrent templating engines. The test1.html template that is located in the src/main/resources folder, can be rendered more than 30 times in a millisecond on a Core i5 CPU.

Exact benchmarks will come later, but if you are impatient, you can run one by yourself and share it on the wiki page.

---
