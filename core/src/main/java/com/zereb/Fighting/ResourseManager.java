package com.zereb.Fighting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;

public class ResourseManager {

    private final HashMap<String, Texture> textures = new HashMap<>();
    private final HashMap<String, TextureAtlas> atlases = new HashMap<>();
    private final HashMap<String, Sound> sounds = new HashMap<>();
    private final HashMap<String, BitmapFont> fonts = new HashMap<>();
    public static ResourseManager INSTANCE = new ResourseManager();
    private final Skin skin;

    public final String PIXEL16 = "pixel16";
    public final String PIXEL = "pixel";


    private ResourseManager(){
        textures.put("fail", new Texture(Gdx.files.internal("_fail_.png")));
        skin = new Skin(Gdx.files.internal("ui/skin.json"));
    }

    public TextureAtlas loadAtlas(String path) {
        if (atlases.containsKey(path))
            return atlases.get(path);

        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) throw new AssertionError(path + " not exists");

        TextureAtlas atlas = new TextureAtlas(fileHandle);

        return loadAtlas(atlas, path);
    }

    private TextureAtlas loadAtlas(TextureAtlas atlas, String path){
        atlases.put(path, atlas);
        Gdx.app.log(getClass().getName(), "loaded texture atlas: " + path);
        return atlas;
    }


    public Texture loadTexture(String path) {
        if (textures.containsKey(path))
            return textures.get(path);

        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) {
            Gdx.app.log(getClass().getName(), "failed to load texture: " + path);
            return textures.get("fail");
        }
        Texture texture = new Texture(fileHandle);


        return loadTexure(texture, path);
    }

    private Texture loadTexure(Texture texture, String path){
        textures.put(path, texture);
        Gdx.app.log(getClass().getName(), "loaded texture: " + path);
        return texture;
    }

    public Sound loadSound(String path){
        if (sounds.containsKey(path))
            return sounds.get(path);

        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) return sounds.get("fail");

        sounds.put(path, Gdx.audio.newSound(fileHandle));
        Gdx.app.log(getClass().getName(), "loaded sound: " + path);
        return sounds.get(path);
    }

    public BitmapFont loadFont(String path){
        if (fonts.containsKey(path))
            return fonts.get(path);

        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) return fonts.put(path, new BitmapFont());

        fonts.put(path, new BitmapFont(fileHandle));
        Gdx.app.log(getClass().getName(), "loaded sound: " + path);
        return fonts.get(path);
    }

    public void dispose(){
        for (Texture value : textures.values())
            value.dispose();
        for (Sound value : sounds.values())
            value.dispose();
        for (BitmapFont value : fonts.values())
            value.dispose();
        for (TextureAtlas value : atlases.values())
            value.dispose();

        sounds.clear();
        textures.clear();
        fonts.clear();
        skin.dispose();
    }

    public Skin skin() {
        return skin;
    }

}
