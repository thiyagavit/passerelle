This project contains a sample empty database instance for a Derby DB server,
for the Passerelle Manager, version 4.1.0.

Remark that for ease of usage in combination with SVN versioning, 
it's better not to launch a Derby server that directly works on this project
in your eclipse workspace.

The preferred approach is to copy this project to a folder outside of your
eclipse workspace, and let the Derby server refer to there.

E.g. in the Passerelle Manager database launcher, specify :

-Dderby.system.home="C:/isencia_dev/test-databases/com.isencia.passerelle.manager.data"

