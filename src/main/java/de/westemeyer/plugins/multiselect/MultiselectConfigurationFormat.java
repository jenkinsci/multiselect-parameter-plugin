package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.ConfigParser;
import de.westemeyer.plugins.multiselect.parser.ConfigSerialization;
import de.westemeyer.plugins.multiselect.parser.CsvParser;
import de.westemeyer.plugins.multiselect.parser.CsvWriter;

import java.util.function.Supplier;

/**
 * Supported configuration formats.
 */
public enum MultiselectConfigurationFormat {
    /** CSV format. */
    CSV(CsvParser::new, () -> {
        return new CsvWriter();
    });

    /** Parser supplier for a format. */
    private final transient Supplier<ConfigParser> parserFactory;

    /** Writer factory for a format. */
    private final transient Supplier<ConfigSerialization> writerFactory;

    /**
     * Create new configuration format instance.
     * @param parserFactory parser factory
     * @param writerFactory serializer factory
     */
    MultiselectConfigurationFormat(Supplier<ConfigParser> parserFactory, Supplier<ConfigSerialization> writerFactory) {
        this.parserFactory = parserFactory;
        this.writerFactory = writerFactory;
    }

    /**
     * Create a new parser using the factory.
     * @return config parser
     */
    public ConfigParser createParser() {
        return parserFactory.get();
    }

    /**
     * Create a new serializer using the factory.
     * @return serializer object
     */
    public ConfigSerialization createWriter() {
        return writerFactory.get();
    }
}
