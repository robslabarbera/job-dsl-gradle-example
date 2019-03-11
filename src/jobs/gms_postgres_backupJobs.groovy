job("gms_postgres_backup") {
	description("pg_basebackup plus copy wal archives to a gpfs filesystem")
	keepDependencies(false)
	disabled(false)
	concurrentBuild(false)
	steps {
		shell("""#!/usr/bin/env bash
set -e
die () {
    echo "\$@"
    exit 1
}
BACKUP_BASE=/vol/aggr13/gc6152/systems/postgres_backup
DATABASE_SERVER=linuscs124
BACKUP_DIR=\${BACKUP_BASE}/genome-\$( date +%Y_%m_%d_%H )
KEEP_DAYS=5
echo "Running on \$(hostname) as:"
id
echo Creating empty backup directories
rm -rf \$BACKUP_DIR/main \$BACKUP_DIR/wal_archive
mkdir -p \$BACKUP_DIR/main \$BACKUP_DIR/wal_archive || die "unable to create directories"
#--backup database--
echo Running pg_basebackup in docker container
docker run -i --rm \\
    -v \$(pwd):/build \\
    -v \$BACKUP_DIR:/backup \\
    postgres:9.2 \\
    pg_basebackup -U postgres -h \$DATABASE_SERVER --no-password \\
        --pgdata=/backup/main --format=p --xlog-method=stream --checkpoint=fast --verbose
#--backup the wal_archive--
echo Copying wal archives with rsync
rsync -av root@\$DATABASE_SERVER:/var/lib/postgresql/wal_archive/* \${BACKUP_DIR}/wal_archive/
echo Cleaning up old archives on database server
ssh root@\$DATABASE_SERVER "find /var/lib/postgresql/wal_archive -type f -daystart -mtime +1 -delete"
# clean old backups
echo Cleaning up old backup directories
pushd "\${BACKUP_BASE}" || die "unable to change to postgres backup directory"
find . -mindepth 1 -maxdepth 1 -type d -daystart -mtime +\${KEEP_DAYS} -exec /bin/rm -rf {} \\; || die "unable to remove old backups"
popd""")
	}
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

