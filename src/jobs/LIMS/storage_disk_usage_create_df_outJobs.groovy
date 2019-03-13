job("storage_disk_usage_create_df_out") {
	
        label('maven') 
        description("""The disk-usage job writes the /gsc/var/cache/disk-usage/df.out file that is a critical portion of the LIMS disk volume management system.

In particular, it reveals what volumes are in each Disk Group.""")
	keepDependencies(false)
	scm {
		git {
			remote {
				github("ssh://git@bitbucket.ris.wustl.edu:7999/sys/systems.git", "ssh")
				credentials("73f749b1-1e33-4874-bcf6-36aa808fdaa5")
			}
			branch("*/master")
		}
	}
	disabled(true)
	concurrentBuild(false)
	steps {
		shell("""#!/bin/bash -l

# It appears this hard codes the location of: /gsc/var/cache/disk-usage/df.out
#/gsc/scripts/sbin/gsc-cron disk-usage --output

# ITDEV-4638: When the `disk-usage` directory was accidentally removed it was
# discovered that `df.out` was just a symlink to `disk-usage.out`.
#ln --symbolic --no-dereference --force /gsc/var/cache/disk-usage/disk-usage.out /gsc/var/cache/disk-usage/df.out

# Make a copy for this jenkins job to archive as a build artifact
#cp /gsc/var/cache/disk-usage/df.out df.out

bash systems.git/GPFS/scripts/generate_df_out.sh
cp df.out /vol/gpfs-home-app/aggr2-app/applications/var/cache/disk-usage/disk-usage.out""")
	}
	publishers {
		archiveArtifacts {
			pattern("df.out")
			allowEmpty(false)
			onlyIfSuccessful(true)
			fingerprint(false)
			defaultExcludes(true)
		}
	}
        triggers {
                             cron("1 1 * * *")
               }
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('10')
				'numToKeep'('10')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
		it / 'properties' / 'com.sonyericsson.rebuild.RebuildSettings' {
			'autoRebuild'('true')
			'rebuildDisabled'('false')
		}
	}
}
