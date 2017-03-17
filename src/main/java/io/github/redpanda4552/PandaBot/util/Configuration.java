package io.github.redpanda4552.PandaBot.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Configuration extends XMLConfiguration {

    private final String[] keyArr = {
        "operator-id",
        "youtube-api-key",
        "discord-token",
        "guild-id"
    };
    
    private final String xmlFilePath = "./pandabot.xml";
    private Configurations cfgs;
    private XMLConfiguration xmlConfig;
    private File xmlFile;
    public boolean ready = false;
    
    /**
     * Create a new config instance. Interally contains the file path and an
     * array of keys the config should contain. If the file does not exist or is
     * missing keys, this constructor will attempt to create the file and
     * populate it with needed keys.
     * @param altConfigPath - Should be null in 98% of cases - the path to an
     * external config file to use. Really should not be used except for tests.
     */
    public Configuration(String altConfigPath) {
        super();
        cfgs = new Configurations();
        
        if (altConfigPath == null) {
            xmlFile = new File(xmlFilePath);
        } else {
            xmlFile = new File(altConfigPath);
        }
        
        if (!xmlFile.exists()) {
            try {
                xmlFile.createNewFile();
                // Apparently Commons Configuration can't handle working with
                // an empty XML file so we need to put something in...
                FileWriter writer = new FileWriter(xmlFile);
                writer.write("<config/>");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        try {
            xmlConfig = cfgs.xml(xmlFile);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return;
        }
        
        if (isMissingKeys()) {
            populate();
        }
        
        ready = true;
    }
    
    public void populate() {
        try {
            FileBasedConfigurationBuilder<XMLConfiguration> fbcb = cfgs.xmlBuilder(xmlFile);
            
            for (String str : keyArr) {
                fbcb.getConfiguration().addProperty(str, " ");
            }
            
            fbcb.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Check if the config loaded successfully.
     * @return False if an exception interrupted config loading, true otherwise.
     */
    public boolean isReady() {
        return ready;
    }
    
    /**
     * Check if all config keys have values assigned.
     * @return True if all config keys have values defined, false otherwise.
     */
    public boolean isComplete() {
        for (String str : keyArr) {
            if (get(str) == null) {
                return false;
            } else if (get(str).trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isMissingKeys() {
        for (String str : keyArr) {
            if (!xmlConfig.containsKey(str)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the value for the specified key from the config file.
     */
    public String get(String key) {
        if (!xmlConfig.containsKey(key)) {
            return null;
        }
        
        return xmlConfig.getString(key);
    }
    
    /**
     * Get the config as a HashMap. If a key isn't defined, it's value will be
     * the null object.
     */
    public HashMap<String, String> getAsMap() {
        HashMap<String, String> ret = new HashMap<String, String>();
        
        for (String str : keyArr) {
            ret.put(str, get(str));
        }
        
        return ret;
    }
}
