# EJCL

EJCL is an advanced configuration library for Java with support for local files, mysql databases, and more.

Bellow you can find information about how to use the library.

EJCL is also fully documented at [ejcl.kaleko.dev/docs](https://ejcl.kaleko.dev/docs/).

## Config types

EJCL has two basic config types, structured and unstructured.

### Structured (Recommended)

Structured configs are created with a Java class used as the "structure".\
Calling `config#get()` will return an instance of the type passed to the constructor for easy use of the object.

### Unstructured

Unstructured configs are not created with a defined "structure" and such can be used to store more dynamic data. (If you are using `MySQLConfig` it also provides a performance boost)\
To get or set data on an unstructured config you can call `config#get(path)` or `config#set(path, value)` to do so (`path` can be any string and `value` can be any object).

## Storage types

EJCL also offers a variety of storage options listed bellow.

### Local filesystem

Configs can be stored locally on the filesystem using classes from the `io.github.kale_ko.ejcl.file` package.

This includes a `SimpleFileConfig` which is an unstructured config that stores key/value pairs in a very simple format `{key}={value}` and may lead to errors if special characters are used.

There is also `BJSLFileConfig` which is a structured config that stores data in different common formats (JSON, Yaml, Toml, XML, CSV, Java Properties, and Json Smile)\
Each of these different formats can be used with their respective classes `{format}FileConfig`

### MySQL Server

Configs can also be stored on a MySQL server using classes from the `io.github.kale_ko.ejcl.mysql` package.

There is both a structured (`StructuredMySQLConfig`) and unstructured (`UnstructuredMySQLConfig`) version to use.\
The structured version is slower to save and load but can be easier to work with.\
The unstructured version also has the advantage of drastically lowering the chances of overwriting a value twice.

### In memory

If you want you can also use EJCL to store a config in memory but there is not much point to this unless you are using an api that only accepts Config objects.

## Contributing

If you would like to contribute I will gladly accept any pull requests to the repo.
