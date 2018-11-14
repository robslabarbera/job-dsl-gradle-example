job("Clean Bamboo XML") {
	description()
	keepDependencies(false)
	disabled(false)
	concurrentBuild(false)
	steps {
		shell("""#!/bin/bash

# remove exited containers:
for I in \$(docker ps --filter status=dead --filter status=exited -aq); do docker rm \$I; done
    

# remove genome + dataxfer images without 'latest' tag
for I in \$(docker images | grep -v latest | grep -e genome -e dataxfer | awk '{ print \$3 }'); do docker rmi -f \$I; done""")
	}
	wrappers {
		timestamps()
	}
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('5')
				'numToKeep'('5')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
	}
}
