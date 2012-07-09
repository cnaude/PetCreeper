/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.entity.EntityType;

/**
 *
 * @author cnaude
 */
public class PetFile {
    
    private final PetMain plugin;
    private File dataFolder;

    private boolean dataFolderExists() {
        this.dataFolder = new File("plugins/PetCreeper");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
        return this.dataFolder.exists();
    }
    
    public PetFile(PetMain instance) {
        this.plugin = instance;

    }
    
    public boolean savePets() {
        if (!dataFolderExists()) {
            System.out.println("Unable to find data folder! [" + this.dataFolder.getAbsolutePath() + "]");
            return false;
        }
        try {
            File petFile = new File(this.dataFolder, "pets.json");            
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Map.Entry<String, ArrayList<Pet>> entry : this.plugin.playersWithPets.entrySet()) {            
                Gson gson = new Gson();
                ArrayList<Pet> pets = entry.getValue();
                for(Iterator i = pets.iterator();i.hasNext();) {
                    Pet pet = (Pet)i.next();                    
                    String json = gson.toJson((Object)pet);
                    out.write(entry.getKey() + "=" + json + "\n");
                }                
            }
            out.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    
    public boolean loadPets() {
        if (!dataFolderExists()) {
            System.out.println("Unable to find data folder! [" + this.dataFolder.getAbsolutePath() + "]");
            return false;
        }
        File creeperFile = new File(this.dataFolder, "pets.txt");
        File creeperFileJson = new File(this.dataFolder, "pets.json");
        if (creeperFile.exists()) {
            System.out.println("Found old pets.txt. Attempting to load pets.");
            try {
                BufferedReader in = new BufferedReader(new FileReader(creeperFile));
                String line;
                String player = "";
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }                    
                    String[] parts = line.split(":", 2);
                    parts[1] = parts[1].replaceAll("^\\s+", "");
                    parts[1] = parts[1].replaceAll("\\s+$", "");
                    if (parts[0].toUpperCase().equals("PLAYER")) {                        
                        player = parts[1];
                        System.out.println("Loading pet for " + player);
                        this.plugin.playersWithPets.put(player, new ArrayList<Pet>());
                    } else if (parts[0].toUpperCase().equals("PETTYPE")) {
                        this.plugin.playersWithPets.get(player).get(0).type = EntityType.fromName(parts[1]);                                                
                    } else if (parts[0].toUpperCase().equals("PETNAME")) {
                        this.plugin.playersWithPets.get(player).get(0).petName = parts[1];
                    } else if (parts[0].toUpperCase().equals("PETHP")) {
                        this.plugin.playersWithPets.get(player).get(0).hp = Integer.parseInt(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SADDLED")) {
                        this.plugin.playersWithPets.get(player).get(0).saddled =  Boolean.parseBoolean(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SHEARED")) {
                        this.plugin.playersWithPets.get(player).get(0).sheared = Boolean.parseBoolean(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SHEEPCOLOR")) {
                        this.plugin.playersWithPets.get(player).get(0).color = Byte.parseByte(parts[1]);
                    } else if (parts[0].toUpperCase().equals("FOLLOWED")) {
                        this.plugin.playersWithPets.get(player).get(0).followed = Boolean.parseBoolean(parts[1]);
                    }                            
                }
                in.close();
                creeperFile.renameTo(new File(this.dataFolder, "pets.txt.old"));
                return true;
            } catch (Exception e) {
            }
        } else if (creeperFileJson.exists()) {
            System.out.println("Found pets.json. Attempting to load pets.");
            Gson gson = new Gson();            
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
                    System.out.println("Loaded pet " + pet.type.getName() + " of " + player);
                }
                return true;                
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }            
        }       
        return false;
    }
}
