/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.datalake.orchestrator.api.model.remote;

public class SFTPCredential {

    public enum AuthMethod {SSH_KEY, PASSWORD}
    private String id;

    private String userName;

    private String existingKeyId;

    private String publicKey;
    private String privateKey;
    private String passphrase;

    private String password;

    private AuthMethod authMethod;

    public String getId() {
        return id;
    }

    public SFTPCredential setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public SFTPCredential setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getExistingKeyId() {
        return existingKeyId;
    }

    public SFTPCredential setExistingKeyId(String existingKeyId) {
        this.existingKeyId = existingKeyId;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public SFTPCredential setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public SFTPCredential setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public SFTPCredential setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SFTPCredential setPassword(String password) {
        this.password = password;
        return this;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public SFTPCredential setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
        return this;
    }
}
