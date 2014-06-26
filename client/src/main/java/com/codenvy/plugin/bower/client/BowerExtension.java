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
package com.codenvy.plugin.bower.client;

import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.resources.model.Folder;
import com.codenvy.ide.api.resources.model.Project;
import com.codenvy.ide.api.resources.model.Resource;
import com.codenvy.ide.api.ui.action.ActionManager;
import com.codenvy.ide.api.ui.action.Constraints;
import com.codenvy.ide.api.ui.action.DefaultActionGroup;
import com.codenvy.ide.extension.builder.client.BuilderLocalizationConstant;
import com.codenvy.plugin.bower.client.menu.BowerInstallAction;
import com.codenvy.plugin.bower.client.menu.LocalizationConstant;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.ui.action.Anchor.AFTER;
import static com.codenvy.ide.api.ui.action.IdeActions.GROUP_BUILD;

/**
 * Extension registering Bower commands
 * @author Florent Benoit
 */
@Singleton
@Extension(title = "Bower extension")
public class BowerExtension {

    @Inject
    public BowerExtension(ActionManager actionManager,
                          BuilderLocalizationConstant builderLocalizationConstant,
                          LocalizationConstant localizationConstantBower,
                          final BowerInstallAction bowerInstallAction,
                          EventBus eventBus) {

        actionManager.registerAction(localizationConstantBower.bowerInstallId(), bowerInstallAction);

        // Get Build menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);

        // create constraint
        Constraints afterBuildConstraints = new Constraints(AFTER, builderLocalizationConstant.buildProjectControlId());

        // Add Bower in build menu
        buildMenuActionGroup.add(bowerInstallAction, afterBuildConstraints);

        // Install Bower dependencies when projects is being opened and that there is no app/bower_components
        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {

                Project project = event.getProject();
                final String projectTypeId = project.getDescription().getProjectTypeId();
                boolean isAngularJSProject = "AngularJS".equals(projectTypeId);
                if (isAngularJSProject) {

                    // Check if there is bower.json file
                    Resource bowerJsonFile = project.findChildByName("bower.json");
                    if (bowerJsonFile != null) {
                        final Resource appDirectory = project.findChildByName("app");
                        if (appDirectory != null && appDirectory instanceof Folder) {
                            project.refreshChildren((Folder) appDirectory, new AsyncCallback<Folder>() {
                                @Override
                                public void onFailure(Throwable caught) {

                                }

                                @Override
                                public void onSuccess(Folder result) {
                                    // Bower configured for the project but not yet initialized ?
                                    Resource bowerComponentsDirectory = ((Folder) appDirectory).findChildByName("bower_components");
                                    if (bowerComponentsDirectory == null) {
                                        // Install bower dependencies as the folder doesn't exist
                                        bowerInstallAction.installDependencies();
                                    }

                                }
                            });
                        } else {
                            // Install bower dependencies as the folder has not been found
                            bowerInstallAction.installDependencies();
                        }
                    }

                }

            }


            @Override
            public void onProjectClosed(ProjectActionEvent event) {

            }

            @Override
            public void onProjectDescriptionChanged(ProjectActionEvent event) {
            }
        });

    }
}
