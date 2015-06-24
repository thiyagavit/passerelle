You need to set a system property -Dorg.passerelle.python.scripts.system=... to point to the folder where the scripts for the Python RPC server are stored.

Original versions of these scripts are included in the scripts folder of this project.
At runtime, Python will not be able to read them as this project will typically be deployed as a jar.
So you need to extract the scripts somewhere first (and point the system property there).