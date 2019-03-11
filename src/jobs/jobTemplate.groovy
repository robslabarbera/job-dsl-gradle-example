job("gms_postgres_backup") {

// 'label' specifies where the job will be built - maven will spawn a new Jenkins agent pod to build the job.
        label('maven')

// Add job description here.
	description("")

	keepDependencies(false)
	disabled(false)
	concurrentBuild(false)

// 'steps' will outline the build process - in this scenario, it will run a shell script.
	steps {
		shell("""#!/usr/bin/env bash

popd""")
	}

// 'timestamps' will add timestamps to the build.
        wrappers {
		timestamps()
	}

// 'triggers' will define the build schedule for the job, using cron syntax.
        triggers {
                             cron("H 0 * * *")
               }

// 'configure' will configure the build archiving - by default, max 100 jobs will be archived dating back 15 days.
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('15')
				'numToKeep'('100')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
	}
}

