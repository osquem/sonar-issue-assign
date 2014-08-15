/*
 * SonarQube Issue Assign Plugin
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.issueassign.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.notifications.Notification;
import org.sonar.api.notifications.NotificationManager;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.DateUtils;
import org.sonar.core.issue.IssuesBySeverity;

import java.util.Date;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.issueassign.IssueAssignPlugin.NOTIFICATION_TYPE_NEW;

@RunWith(MockitoJUnitRunner.class)
public class IssueNotificationsTest {

  @Mock
  NotificationManager manager;

  IssueNotifications issueNotifications;

  @Before
  public void setUp() throws Exception {
    issueNotifications = new IssueNotifications(manager);
  }

  @Test
  public void should_send_new_issues() throws Exception {
    Date date = DateUtils.parseDateTime("2013-05-18T13:00:03+0200");
    Project project = new Project("struts").setAnalysisDate(date);
    IssuesBySeverity issuesBySeverity = mock(IssuesBySeverity.class);
    when(issuesBySeverity.size()).thenReturn(42);
    when(issuesBySeverity.issues("MINOR")).thenReturn(10);
    Map<String, IssuesBySeverity> issuesByUser = ImmutableMap.of("user1", issuesBySeverity);

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);

    issueNotifications.sendIssues(project, issuesByUser, NOTIFICATION_TYPE_NEW);

    verify(manager).scheduleForSending(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertThat(notification.getFieldValue("count")).isEqualTo("42");
    assertThat(notification.getFieldValue("count-MINOR")).isEqualTo("10");
    assertThat(DateUtils.parseDateTime(notification.getFieldValue("projectDate"))).isEqualTo(date);
  }

}
