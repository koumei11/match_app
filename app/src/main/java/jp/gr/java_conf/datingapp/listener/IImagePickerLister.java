package jp.gr.java_conf.datingapp.listener;

import jp.gr.java_conf.datingapp.enums.ImageEnum;

@FunctionalInterface
public interface IImagePickerLister {
    void onOptionSelected(ImageEnum imageEnum);
}
