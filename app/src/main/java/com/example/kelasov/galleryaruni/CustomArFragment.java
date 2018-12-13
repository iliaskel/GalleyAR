package com.example.kelasov.galleryaruni;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {


    @Override
    protected Config getSessionConfiguration(Session session) {

        Config config = new Config(session);
        config.setFocusMode(Config.FocusMode.AUTO);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        session.configure(config);
        this.getArSceneView().setupSession(session);

        return config;

    }
}
