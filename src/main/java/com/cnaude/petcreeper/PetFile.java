package com.cnaude.petcreeper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;

/**
 *
 * @author cnaude
 */
public class PetFile {
    
    private final PetCreeper plugin;
    private File dataFolder;

    private boolean dataFolderExists() {
        this.dataFolder = new File("plugins/PetCreeper");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
        return this.dataFolder.exists();
    }
    
    public PetFile(PetCreeper instance) {
        this.plugin = instance;
    }
    
    public boolean savePets() {
        boolean saved;
        if (!dataFolderExists()) {
            plugin.logInfo("Unable to find data folder! [" + this.dataFolder.getAbsolutePath() + "]");
            return false;
        }        
        try {
            File petFile = new File(this.dataFolder, "pets.json");            
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Map.Entry<String, ArrayList<Pet>> entry : this.plugin.playersWithPets.entrySet()) {                           
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                ArrayList<Pet> pets = entry.getValue();
                for (Pet pet : pets) {                    
                    String json = gson.toJson((Object)pet);
                    out.write(entry.getKey() + "=" + json + "\n");
                }                
            }
            out.close();
            saved = true;
        } catch (Exception e) {
            plugin.logInfo(e.getMessage());
            saved = false;
        }
        return saved;
    }
    
    public boolean loadPets() {
        if (!dataFolderExists()) {
            plugin.logInfo("Unable to find data folder! [" + this.dataFolder.getAbsolutePath() + "]");
            return false;
        }        
        File creeperFileJson = new File(this.dataFolder, "pets.json");
        if (creeperFileJson.exists()) {
            plugin.logInfo("Found pets.json. Attempting to load pets.");
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            try {             
                BufferedReader in = new BufferedReader(new FileReader(creeperFileJson));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }                    
                    String[] parts = line.split("=", 2);
                    String player = parts[0];
                    String json = parts[1];
                    Pet pet = gson.fromJson(json, Pet.class);
                    if (!this.plugin.playersWithPets.containsKey(player)) {
                        this.plugin.playersWithPets.put(player, new ArrayList<Pet>());
                    }
                    this.plugin.playersWithPets.get(player).add(pet); 
                    plugin.logInfo("Loaded pet " + pet.type.getName() + " of " + player);
                }
                return true;                
            } catch (Exception e) {
                plugin.logInfo(e.getMessage());
            }            
        }       
        return false;
    }
    
    public void loadNames() {
        int count = 0;
        for (String fileName : PetConfig.nameFiles) {
            File file = new File(this.dataFolder + "/" + fileName);
            if (!file.exists()) {
                try {
                    InputStream in = PetCreeper.class.getResourceAsStream("/" + fileName);
                    byte[] buf = new byte[1024];
                    int len;
                    OutputStream out = new FileOutputStream(file);
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                } catch (Exception ex) {
                    plugin.logInfo(ex.getMessage());
                }
            }
        }
        for (String fileName : PetConfig.nameFiles) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(this.dataFolder + "/" + fileName)); 
                String line = br.readLine();

                while (line != null) {                    
                    if (!line.matches("^#")) {                        
                        plugin.bigNamesList.add(line);
                        count++;
                    }
                    line = br.readLine();
                }
            }
            catch (Exception ex) {
                plugin.logInfo("Unable to load " + fileName + " [" + ex.getMessage() + "]");
            }
        
        }
        plugin.logInfo("Pet names loaded: " + count);
    }
}
