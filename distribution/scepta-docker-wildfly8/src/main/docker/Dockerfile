#
# Copyright (C) 2015 Red Hat, Inc. and/or its affiliates
# and other contributors as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM jboss/wildfly:8.1.0.Final

RUN $JBOSS_HOME/bin/add-user.sh admin admin123! --silent

EXPOSE 8787

ADD scepta-docker-wildfly8.zip /tmp/

RUN cd $HOME \
    && bsdtar -xf /tmp/scepta-docker-wildfly8.zip \
    && cd scepta-docker-wildfly8 \
    && cp scepta-server.war $JBOSS_HOME/standalone/deployments \
    && cp scepta-ui.war $JBOSS_HOME/standalone/deployments \
    && cd $HOME \
    && rm -rf scepta-docker-wildfly8

ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh"]
CMD ["-c", "standalone.xml", "--debug"]
