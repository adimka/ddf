/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */

package org.codice.ddf.configuration.store;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Base class for configuration file objects. Configuration file objects can be created using the
 * {@link ConfigurationFileFactory} class.
 */
public abstract class ConfigurationFile {

    protected final Dictionary<String, Object> properties;

    protected final ConfigurationAdmin configAdmin;

    protected final Path configFilePath;

    protected final PersistenceStrategy persistenceStrategy;

    /**
     * Constructor called by {@link ConfigurationFileFactory}. Assumes that none of the parameters
     * are {@code null}.
     *
     * @param configFilePath      path to the configuration file
     * @param properties          properties associated with the configuration file
     * @param configAdmin         reference to OSGi's {@link ConfigurationAdmin}
     * @param persistenceStrategy how to write out the file {@link PersistenceStrategy}
     */
    ConfigurationFile(Path configFilePath, Dictionary<String, Object> properties,
            ConfigurationAdmin configAdmin, PersistenceStrategy persistenceStrategy) {
        this.configAdmin = configAdmin;
        this.properties = properties;
        this.configFilePath = configFilePath;
        this.persistenceStrategy = persistenceStrategy;
    }

    /**
     * Gets the configuration file location
     *
     * @return configuration file location
     */
    public Path getConfigFilePath() {
        return configFilePath;
    }

    public abstract void createConfig() throws ConfigurationFileException;

    public void exportConfig(String destination) throws IOException {
        persistenceStrategy.write(new FileOutputStream(destination), properties);
    }

    /**
     * Provides a convenient way to construct {@link ConfigurationFile}.
     *
     * @param configAdmin         reference to OSGi's {@link ConfigurationAdmin}
     * @param persistenceStrategy how to write out the file {@link PersistenceStrategy}
     */
    public abstract static class ConfigurationFileBuilder {
        protected ConfigurationAdmin configAdmin;

        protected Dictionary<String, Object> properties;

        protected Path configFilePath;

        protected PersistenceStrategy persistenceStrategy;

        public ConfigurationFileBuilder(ConfigurationAdmin configAdmin,
                PersistenceStrategy persistenceStrategy) {
            this.configAdmin = configAdmin;
            this.persistenceStrategy = persistenceStrategy;
            this.reset();
        }

        public ConfigurationFileBuilder reset() {
            this.properties = null;
            this.configFilePath = null;
            return this;
        }

        public ConfigurationFileBuilder properties(Dictionary<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public ConfigurationFileBuilder configFilePath(Path configFilePath) {
            this.configFilePath = configFilePath;
            return this;
        }

        public abstract ConfigurationFile build();
    }
}
