templating-web
==============

## Introduction

Everit Web Templating can be used to render web pages based on HTML
templates. The library can also be used to generate XMLs as XML syntax
is compatible with HTML.

The goals of the templating engine are:

 - Be extremely fast
 - Have a very small binary and memory footprint
 - Do not do more just HTML templating (No MVC, URL resolving, etc.)
 - The output is as close to the original template as possible. All of
   the linebreaks and whitespaces are left there. Bugfixing of a template
   is much easier in this way. In case you want to save bandwidth, use a
   ZIP stream. Three whitespaces will cost the almost the same as one. 
 - Allow the programmers to use the code created by web designers as it
   is delivered. Only decoration should with templating specific attributes
   should be done, so the web designer can continue working on the code.

## Usage

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

## Attributes

### foreach

Iterates through the elements of an Iterable element, Object array or
primitive array. The value of the attribute must be a Map where the key
is the name of the variable that holds the element of the iteration, the
value holds the collection that is iterated. Example based on MVEL:

    <table>
      <tr data-eht-foreach="['user' : users]">
        <td data-eht-text="user.firstName">John</td>
        <td data-eht-text="user.lastName">Doe</td>
      </tr>
    </table>

An array of two Strings can be defined as the key of the Map. In that case the
first element of the array will show the name of the variable of the element,
while the second String will be the name of the index variable. Example
based on MVEL expressions:

    <table>
      <tr data-eht-foreach="[({'user', 'index'}) : users]">
        <td data-eht-text="index + 1">1</td>
        <td data-eht-text="user.firstName">John</td>
        <td data-eht-text="user.lastName">Doe</td>
      </tr>
    </table>

Multi-iteration is also supported when the Map has more entries. Use this
only if you are sure that the Map keeps its insertion order, otherwise
the output might be "randomized".

### code

It is possible to run a code block. In most cases it is necessary to
declare new variables.

    <p data-eht-code="var myVar1 = 'Hello'; var myVar2 = myVar1 + ' world!'"
       data-eht-text="myVar2" />

The code snippet above will write out

    <p>Hello world!</p>

### var

Sometimes it is not possible to declare variables with the expression
language that are put into the variables map. That is where _var_ helps.

With _var_ it is possible to declare / assigne one or more variables.
The value of the attribute must be a Map, where the keys of the Map are the
name of the variables and the values of the Map are the values of the
variables. Example based on MVEL:

    <div data-eht-var="['firstName' : user.firstName,
                        'lastName' : user.lastName]">
        ...
    </div>

### render

A case-insensitive String value that tells which parts of the element
should be rendered:

 - __all__: The element and its body is rendered. This is the default.
 - __content__: Only the body of the element is rendered, the element itself
   will not be part of the output
 - __tag__: Only the element itself is rendered, its content is not
 - __none__: The element is not rendered at all
 
 Boolean values can also be used in the following way:
 
  - __true__: The same as _all_
  - __false__: The same as _none_

The value of the _render_ attribute can be defined dynamically:

    <span data-eht-render="user.firstName == 'John' ? 'all' : 'none'">...</span>

In case _none_ is specified as a constant, the parsing of the element and
its content is skipped completely. This is useful if we want to comment
something out from the template in the way that it is not processed:

    <span data-eht-render="'none'">...</span>

###text

Replaces the content of the element with the result of the evaluation of the
value of the attribute. The text will be escaped.

    <td data-eht-text="user.firstName">John</td>

This attribute should be used also if the content of the tag is written with
a function call directly to the writer. E.g.:

    <td data-eht-text="appender.writeOnWriter()">John</td>

In the example above, the writeOnWriter might be a void function that writes
onto the same writer that is passed to the CompiledTemplate.render(...)
function.

### utext

Same as text, but the text will not be escaped.

### attr

A Map of attribute overrides.

    <div id="testId" data-eht-attr="['id' : 'runtimeId', 'class' : 'someClass']" />

The output of the template above will be the following:

    <div id="runtimeId" class="someClass" />

### attr-*

It possible to override the value of any attribute by defining a new value.

    <span id="testId" data-eht-attr-id="'runtimeId'" />

### attrprepend

Prepends the value of one or more attributes. A Map should be provided where
the keys are the name of the attributes and the values of the Map are the new
values.

    <div class="class2"
         data-eht-attrprepend="['runtimeId' : 'runtime',
                                'class' : 'class1 ']" />

The output of the template above will be the following:

    <div class="class1 class2" id="runtimeId" />

### attrprepend-*

As with _attr-*_, it is possible to prepend attributes. A String should
be provided!

    <div id="Id" data-eht-attrprepend-id="'runtime'" />

The output of the template above will be the following:

    <div id="runtimeId" /> 

### attrappend

Same as _attrPrepend_, but it appends the newly defined values to the end
of the original attributes.

### attrappend-*

Same as _attrprepend-*_, but it appends the newly defined value to the end
of the original attribute.

### inline:

It is possible to mix the syntax of the template with other template syntaxes.
This is useful if some other format should be embedded whithin a template,
e.g.: a javascript.

Inline template compilers must be defined during the initialization of the
HTML template compiler.

    // Instantiate any expression compiler (e.g.: MVEL)
    ExpressionCompiler expressionCompiler = new MvelExpressionCompiler();
    
    // Create the inline compilers and put them into a Map
    TemplateCompiler textCompiler = new TextTemplateCompiler(expressionCompiler);
    
    Map<String, TemplateCompiler> inlineCompilers = new HashMap<>();
    inlineCompilers.put("text", textCompiler);

    // Instantiate template compiler and pass the expression compiler
    HTMLTemplateCompiler compiler = new HTMLTemplateCompiler(expressionCompiler);

    // Compile and render the template...

In the example above one inline compiler was defined with the _text_
identifier. In the template the same identifier should be used within
the _fragment_ attribute as a constant String.

    <script type="text/javascript" data-eht-inline="'text'">
      var firstName = @{user.firstName};
    </script>

### fragment

It is possible to define fragments that can be rendered separately, too.

    <div data-eht-fragment="'myFragment'">
      This is the content of the fragment
    </div>

The fragment can be rendered programmatically:

    template.render(writer, vars, "myFragment");

It is also possible to render a fragment from any place of the template
that defined it:

    <div data-eht-utext="template_ctx.renderFragment('myFragment')" />

Let's say that a fragment should be rendered only if it is called:

    <div data-eht-fragment="'myFragment'"
         data-eht-render="template_ctx.fragmentId == 'myFragment'">
      This is the content of the fragment
    </div>

## Replacing the attribute prefix

By default _data-eht-_ is the prefix for the templating attributes. This
is good for HTML as in HTML any attribute starting with _data-_ are allowed.

However, in XML someone might wants to use an XML namespace instead. The
default prefix can be overridde in the constructor of HTMLTemplateCompiler.

    new HTMLTemplateCompiler(expressionCompiler, inlineCompilers, "eht:");

In the XML, the namespace can be defined with the prefix above. In case
the namespace definition should be hidden in the output, it can be deleted
by overriding the value of the namespace declaration to null with the
_attr_ attribute.

    <myElement xmlns:eht="http://ehtNamespace"
               eht:attr="['xmlns:eht' : null]">
       ....
    </myElement>


## Specialties

### Works with XML

Web Templating can be used to generate XML documents as XML has stricter
rules than HTML.

    // TODO example of using custom namespace.

### Works with unclosed elements

In XML all tags must be closed. However, in HTML the same rules are not
applied, it is possible to use a tag without closing it. E.g.:

    <div><span>MyText</div>

### Extremely fast

Everit HTML templating is often 7-10x faster than concurrent templating
engines. The test1.html template that is located in the src/main/resources
folder, can be rendered more than 30 times in a millisecond on a Core i5
CPU.

Exact benchmarks will come later, but if you are impatient, you can run
one by yourself and share it on the wiki page.

