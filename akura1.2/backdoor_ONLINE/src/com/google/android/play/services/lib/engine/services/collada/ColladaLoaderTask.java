package com.google.android.play.services.lib.engine.services.collada;

import android.content.Context;
import android.net.Uri;

import com.google.android.play.services.lib.engine.model.Object3DData;
import com.google.android.play.services.lib.engine.services.LoaderTask;
import com.google.android.play.services.lib.engine.services.collada.entities.AnimatedModelData;
import com.google.android.play.services.lib.engine.services.collada.loader.ColladaLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ColladaLoaderTask extends LoaderTask {

    AnimatedModelData modelData;

    public ColladaLoaderTask(Uri uri, Callback callback) {
        super(uri, callback);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object3DData> build() throws IOException {
        // Parse STL
        Object[] ret = ColladaLoader.buildAnimatedModel(new URL(uri.toString()));
        List<Object3DData> datas = (List<Object3DData>) ret[1];
        modelData = (AnimatedModelData) ret[0];
        return datas;
    }

    @Override
    protected void build(List<Object3DData> datas) throws Exception {
        ColladaLoader.populateAnimatedModel(new URL(uri.toString()), datas, modelData);
        if (datas.size() == 1) {
            datas.get(0).centerAndScale(5, new float[]{0, 0, 0});
        } else {
            Object3DData.centerAndScale(datas, 5, new float[]{0, 0, 0});
        }
    }

}
