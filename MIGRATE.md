# Migration Guide

## 4.x to 5.x

It is recommended to load your config using the [`LegacyDefaultTypeProcessors`](https://ejcl.kaleko.dev/docs/io/github/kale_ko/ejcl/legacy/LegacyDefaultTypeProcessors.html) first and then save it using the [`DefaultTypeProcessors`](https://bjsl.kaleko.dev/docs/io/github/kale_ko/bjsl/processor/DefaultTypeProcessors.html). An example for that using the [`StructuredSimpleFileConfig`](https://ejcl.kaleko.dev/docs/io/github/kale_ko/ejcl/file/bjsl/StructuredSimpleFileConfig.html) is as follows.

```java
import java.io.IOException;
import java.nio.file.Path;

import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.file.simple.StructuredSimpleFileConfig;
import io.github.kale_ko.ejcl.legacy.LegacyDefaultTypeProcessors;

public class Migrate {
    public static class Data {
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

        ObjectProcessor.Builder oldProcessorBuilder = new ObjectProcessor.Builder();  // Create a processor with default options
        LegacyDefaultTypeProcessors.register(oldProcessorBuilder); // Register the legacy default type processors on the processor
        ObjectProcessor oldProcessor = oldProcessorBuilder.build();

        StructuredSimpleFileConfig<Data> oldConfig = new StructuredSimpleFileConfig.Builder<>(Data.class, file).setProcessor(oldProcessor).build(); // Create a StructuredSimpleFileConfig with the old processor

        ObjectProcessor newProcessor = new ObjectProcessor.Builder().build(); // Create a processor with default options

        StructuredSimpleFileConfig<Data> newConfig = new StructuredSimpleFileConfig.Builder<>(Data.class, file).setProcessor(newProcessor).build(); // Create a StructuredSimpleFileConfig with the new processor

        oldConfig.load(); // Load the config from file using the old type processors
        newConfig.set(oldConfig.get()); // Move the data from the old config to the new config
        // newConfig.revalidateCache(); // If using a StructuredMySQLConfig, revalidate the cache
        oldConfig.close(); // Close the old config
        newConfig.save(); // Save the config to file using the new type processors
        newConfig.close(); // Close the new config
    }
}
```