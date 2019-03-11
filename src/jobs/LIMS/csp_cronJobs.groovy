job("csp-cron") {
	description("confirm_scheduled_pse_cron")
	label('maven')
        keepDependencies(false)
	disabled(false)
	concurrentBuild(false)
	steps {
		shell("""#!/bin/bash

source /opt/lsf9/conf/profile.lsf

export LSB_JOB_REPORT_MAIL=N
export LIMS_MIN_LOG_LEVEL=info
export LANG=C
export LSB_DOCKER_MOUNT_GSC=1
echo LIMS_DEFAULT_DOCKER_IMAGE="\${LIMS_DEFAULT_DOCKER_IMAGE}"


echo 'bsub -a "docker(\${LIMS_DEFAULT_DOCKER_IMAGE})" -q lims-pipeline -R rusage[db_dw_prod=1] -I /usr/local/bin/lims-env perl -MCarp::Always -S confirm_scheduled_pse_cron '
bsub  -a "docker(\${LIMS_DEFAULT_DOCKER_IMAGE})" -q lims-pipeline -R rusage[db_dw_prod=1] -I /usr/local/bin/lims-env perl -MCarp::Always -S confirm_scheduled_pse_cron 
exit \$?""")
	}
	publishers {
		extendedEmail {
			recipientList("mgi-lims@gowustl.onmicrosoft.com")
			contentType("default")
			defaultSubject("\$DEFAULT_SUBJECT")
			defaultContent("\$DEFAULT_CONTENT")
			attachmentPatterns()
			preSendScript("\$DEFAULT_PRESEND_SCRIPT")
			attachBuildLog(false)
			compressBuildLog(false)
			replyToList("\$DEFAULT_REPLYTO")
			saveToWorkspace(false)
			disabled(false)
		}
	}
	wrappers {
		timeout {
			absolute(10)
		}
	}
        triggers {
                             cron("0-59/2 4-22 * * *")
               }
        
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('-1')
				'numToKeep'('15')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
		it / 'properties' / 'com.sonyericsson.rebuild.RebuildSettings' {
			'autoRebuild'('false')
			'rebuildDisabled'('false')
		}
	}
}
