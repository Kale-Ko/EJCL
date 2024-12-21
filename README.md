# EJCL

EJCL is an advanced configuration library for Java with support for local files, MySQL databases, and more.

EJCL is fully documented at [ejcl.kaleko.dev/docs](https://ejcl.kaleko.dev/docs/)

## Config types

EJCL has two basic config types, structured and unstructured.

### Structured (Recommended)

Structured configs are created with a Java class used as the "structure".\
Calling `config#get()` will return an instance of the type passed to the constructor for easy use of the object.

### Unstructured

Unstructured configs are not created with a defined "structure" and such can be used to store more dynamic data.\
To get or set data on an unstructured config you can call `config#get(path)` or `config#set(path, value)` to do so (`path` can be any string and `value` can be any primitive).

## Storage types

EJCL also offers a variety of storage options listed below.

### Local filesystem

Configs can be stored locally on the filesystem using classes from the `io.github.kale_ko.ejcl.file` package.

This includes a `SimpleFileConfig` which is a config that stores key/value pairs in a very simple format, `{key}={type}={value}`.

There is also `BJSLFileConfig` which is a config that stores data in a few common formats (JSON, YAML, and Json Smile).\
Each of these different formats can be used by passing their respective parsers (`{format}Parser`) to the constructor.

### MySQL/MariaDB Server

Configs can also be stored on a MySQL or MariaDB server using classes from the `io.github.kale_ko.ejcl.mysql` package.

The structured version is slower to load but can be easier to work with, while the unstructured version has the advantage of only fetching the data you need.

Note: The structured version only writes changed values to prevent overwriting others changes.

### In memory

If you want you can also use EJCL to store a config in memory but there is not much point to this unless you are using an API that only accepts Config objects.

## Type Processors

Type processors are a way to convert between any Java type and object trees for storage. They can be created using `Builder#setProcessor(new ObjectProcessor.Builder().setX(y).build())`

See [https://github.com/Kale-Ko/BJSL](https://github.com/Kale-Ko/BJSL?tab=readme-ov-file#type-processors) for information.

## Annotations and Conditions

Annotations and conditions are just ways to tell the processor what to serialize and what not to.

See [https://github.com/Kale-Ko/BJSL](https://github.com/Kale-Ko/BJSL?tab=readme-ov-file#annotations-and-conditions) for information.

## Full Example

```java
import java.io.IOException;
import java.nio.file.Path;

import io.github.kale_ko.bjsl.parsers.JsonParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig;

public class Example {
    public static class Data { // For more see https://github.com/Kale-Ko/BJSL?tab=readme-ov-file#full-example
        private double foo;
        private double bar = 17.8;

        public double getFoo() {
            return foo;
        }

        public double getBar() {
            return bar;
        }
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("config.json");

        JsonParser parser = new JsonParser.Builder().setPrettyPrint(true).build(); // Create a parser with default options
        ObjectProcessor processor = new ObjectProcessor.Builder().build(); // Create a processor with default options

        StructuredBJSLFileConfig<Data> config = new StructuredBJSLFileConfig.Builder<>(Data.class, file, parser).setProcessor(processor).build(); // Create a StructuredBJSLFileConfig with our processor

        config.load(); // Load the config from file

        System.out.println("foo: " + config.get().foo); // Read some values
        System.out.println("bar: " + config.get().bar);

        config.get().foo += 5.0; // Modify a value

        config.save(); // Save the config back to file

        config.close(); // Finally close the config
    }
}
```