package org.eden.epsilon.modules;

import org.eclipse.epsilon.egl.EglModule;

// concrete module store for EGL
public class EglModuleStore extends BaseModuleStore<EglModule> {
    
    public static final EglModuleStore instance = new EglModuleStore();
    
    public EglModuleStore() {
        super("generations", ".egl", EglModule.class);
    }
}