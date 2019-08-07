package br.com.obviobrasil;
/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.common.collect.Lists;

/**
 * Create a video bulletin that is posted to the user's channel feed.
 *
 * @author Jeremy Walker
 */
public class ChannelList {
	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 * @param scopes list of scopes needed to run upload.
	 */
	private static Credential getCredential(List<String> scopes) throws Exception {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(ChannelList.class.getResourceAsStream("/client_secrets.json")));
		final String home = System.getProperty("user.home");
		final File store = new File(home, ".credentials/channel-list.json");
		FileCredentialStore credentialStore = new FileCredentialStore(store, JSON_FACTORY);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, scopes).setAccessType("offline").setCredentialStore(credentialStore).build();
		LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
		return credential;
	}

	/**
	 * Authorize the user, call the youtube.channels.list method to retrieve
	 * information about the user's YouTube channel, and post a bulletin with a
	 * video ID to that channel.
	 */
	public static void main(String[] args) {
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email");
		try {
			final Credential credential = getCredential(scopes);
//			final String token = "ya29.GltdB9iWKe5kCDdfSSOHyMBU5vn3IqHfuHepJyrL4cpilV8mMvnlZ4gUKQ-_MPlCsASKIGH3P9xVIbJaDCcEmJp24JdLETjFtGeSJpNnOI2YFt_aUnLvPWHh-FUu";
			YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Hugme")
					.build();
			YouTube.Channels.List channelRequest = youtube.channels().list("id,snippet,contentDetails");
			channelRequest.setMine(true);
			ChannelListResponse channelResult = channelRequest.execute();
			List<Channel> channelsList = channelResult.getItems();
			if (channelsList != null) {
				for (Channel channel : channelsList) {
					final ChannelSnippet snippet = channel.getSnippet();
					System.out.println(channel.getId() + ": " + snippet.getTitle());
				}
			} else {
				System.out.println("No channels are assigned to this user.");
			}
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println(
					"There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
