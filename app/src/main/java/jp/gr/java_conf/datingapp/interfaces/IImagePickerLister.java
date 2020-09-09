package jp.gr.java_conf.datingapp.interfaces;

import jp.gr.java_conf.datingapp.enums.ImageEnum;

@FunctionalInterface
public interface IImagePickerLister {
    void onOptionSelected(ImageEnum imageEnum);
}
