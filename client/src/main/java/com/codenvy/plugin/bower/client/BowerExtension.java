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

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.api.project.shared.dto.TreeElement;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.Constraints;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.extension.builder.client.BuilderLocalizationConstant;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.plugin.bower.client.menu.BowerInstallAction;
import com.codenvy.plugin.bower.client.menu.LocalizationConstant;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.action.Anchor.AFTER;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;


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
                          EventBus eventBus,
                          final ProjectServiceClient projectServiceClient,
                          final DtoUnmarshallerFactory dtoUnmarshallerFactory) {

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

                final ProjectDescriptor project = event.getProject();
                final String projectTypeId = project.getProjectTypeId();
                boolean isAngularJSProject = "AngularJS".equals(projectTypeId);
                if (isAngularJSProject) {

                    // Check if there is bower.json file
                    projectServiceClient.getFileContent(project.getPath() + "bower.json", new AsyncRequestCallback<String>() {
                        @Override
                        protected void onSuccess(String result) {
                            Unmarshallable<TreeElement> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(TreeElement.class);
                            projectServiceClient.getTree(project.getPath(), 2, new AsyncRequestCallback<TreeElement>(unmarshaller) {
                                @Override
                                protected void onSuccess(TreeElement treeElement) {
                                    ItemReference appDirectory = null;
                                    ItemReference bowerComponentsDirectory = null;
                                    for (TreeElement element : treeElement.getChildren()) {
                                        if ("app".equals(element.getNode().getName()) && "folder".equals(element.getNode().getType())) {
                                            appDirectory = element.getNode();
                                            for (TreeElement e : element.getChildren()) {
                                                if ("bower_components".equals(e.getNode().getName()) && "folder".equals(
                                                        e.getNode().getType())) {
                                                    bowerComponentsDirectory = e.getNode();
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }

                                    if (appDirectory != null) {
                                        // Bower configured for the project but not yet initialized ?
                                        if (bowerComponentsDirectory == null) {
                                            // Install bower dependencies as the 'app/bower_components' folder doesn't exist
                                            bowerInstallAction.installDependencies();
                                        }
                                    } else {
                                        // Install bower dependencies as the 'app' folder has not been found
                                        bowerInstallAction.installDependencies();
                                    }
                                }

                                @Override
                                protected void onFailure(Throwable ignore) {
                                    // nothing to do
                                }
                            });
                        }

                        @Override
                        protected void onFailure(Throwable ignore) {
                            // nothing to do
                        }
                    });
                }
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {

            }

        });

    }
}
