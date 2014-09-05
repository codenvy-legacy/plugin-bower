/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.bower.client.menu;


import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;

/**
 * Allow to hide elements if the current project is not an angular project.
 * @author Florent Benoit
 */
public abstract class CustomAction extends Action {

    private AppContext appContext;

    public CustomAction(AppContext appContext, String name, String description) {
        super(name, description, null);
        this.appContext = appContext;
    }

        /** {@inheritDoc} */
        @Override
        public void update (ActionEvent e){
            CurrentProject activeProject = appContext.getCurrentProject();
            if (activeProject != null) {
                final String projectTypeId = activeProject.getProjectDescription().getProjectTypeId();
                boolean isJSProject = projectTypeId.endsWith("JS");
                e.getPresentation().setVisible(isJSProject);
            } else {
                e.getPresentation().setEnabledAndVisible(false);
            }
        }
    }
