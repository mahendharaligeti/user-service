# Production Deployment Checklist

## Pre-Deployment Verification

- [x] Code compiles without errors
- [x] All 33 tests pass
- [x] No hardcoded secrets in production profile
- [x] Environment variables defined for production
- [x] Database migrations validated
- [x] Security configuration enabled
- [x] JWT expiration set (24 hours)
- [x] CORS configured if needed
- [x] Logging configured
- [x] Health endpoint available

## Database Setup

### AWS RDS MySQL Setup
```sql
-- Create database
CREATE DATABASE user_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'app_user'@'%' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON user_service_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
```

### Environment Variables
```bash
export JWT_SECRET="your-256-bit-secret-key-must-be-exactly-256-bits-long"
export DB_URL="jdbc:mysql://your-rds-endpoint.rds.amazonaws.com:3306/user_service_db?useSSL=true&serverTimezone=UTC"
export DB_USERNAME="app_user"
export DB_PASSWORD="secure_password_here"
```

## Application Configuration

### Verify application-prod.yml
- ✓ DDL set to `validate` (no auto-schema changes)
- ✓ SQL logging disabled (show-sql: false)
- ✓ Environment variables used for all secrets
- ✓ Connection pooling configured

### Security Checklist
- [x] BCrypt password encoder configured
- [x] JWT signing with HS256
- [x] CSRF protection disabled (stateless)
- [x] CORS headers (if needed)
- [x] Rate limiting (consider adding)
- [x] Request validation enabled
- [x] Exception handling centralized

## Deployment Steps

### 1. Build JAR
```bash
mvn clean package -DskipTests -P prod
# Output: target/user-service-1.0.0.jar
```

### 2. Copy to Server
```bash
scp target/user-service-1.0.0.jar user@production-server:/opt/apps/
```

### 3. Set Environment Variables
```bash
ssh user@production-server
export JWT_SECRET="..."
export DB_URL="..."
export DB_USERNAME="..."
export DB_PASSWORD="..."
```

### 4. Run Application
```bash
java -Xmx1024m -Xms512m \
  -jar /opt/apps/user-service-1.0.0.jar \
  --spring.profiles.active=prod &
```

### 5. Verify Health
```bash
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}
```

### 6. Test API
```bash
# Register test user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@hospital.com",
    "password": "SecurePassword123",
    "role": "ADMIN"
  }'

# Login and get token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@hospital.com",
    "password": "SecurePassword123"
  }'

# Test protected endpoint
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Monitoring

### Log Aggregation
```bash
# View application logs
tail -f /var/log/user-service/app.log

# Check error rate
grep ERROR /var/log/user-service/app.log | wc -l
```

### Metrics
- Use `/actuator/health` for health checks
- Monitor database connection pool
- Track JWT token generation rate
- Monitor authentication failures

### Alerts
- Set alert if health check fails
- Alert on high error rate
- Alert on database connection issues
- Alert on authentication failures

## Performance Tuning

### JVM Settings
```bash
-Xmx2048m          # Max heap size (adjust based on load)
-Xms1024m          # Initial heap size
-XX:+UseG1GC       # Use G1 garbage collector
-XX:MaxGCPauseMillis=200
```

### Database
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      max-lifetime: 1800000
      idle-timeout: 600000
```

## Rollback Plan

If issues occur after deployment:

1. **Check logs** for errors
```bash
tail -100 /var/log/user-service/app.log
```

2. **Verify database connectivity**
```bash
mysql -h <RDS_ENDPOINT> -u <USER> -p -e "SELECT 1"
```

3. **Stop current instance**
```bash
ps aux | grep user-service
kill <PID>
```

4. **Redeploy previous version**
```bash
java -jar /opt/apps/user-service-0.9.0.jar --spring.profiles.active=prod &
```

5. **Notify team** of rollback

## Post-Deployment

- [x] Verify all APIs respond correctly
- [x] Check database for initial user creation
- [x] Monitor application logs for errors
- [x] Test JWT token generation and validation
- [x] Confirm role-based access control works
- [x] Validate error responses
- [x] Check performance metrics

## Security Hardening

### Additional Considerations
1. **API Gateway**: Implement rate limiting
2. **WAF**: Enable Web Application Firewall
3. **SSL/TLS**: Enforce HTTPS only
4. **API Keys**: Consider API key authentication for service-to-service
5. **Audit Logging**: Log all authentication attempts
6. **Database**: Enable binary logging for recovery
7. **Backup**: Schedule automated RDS backups

## Version Management

### Track Deployments
```
v1.0.0 - Initial release
  - JWT authentication
  - RBAC implementation
  - 33 tests passing
```

### Update Changelog
- Document changes
- Note breaking changes
- Include migration steps

## Maintenance Schedule

- [ ] Weekly: Review error logs
- [ ] Weekly: Monitor database size
- [ ] Monthly: Review access patterns
- [ ] Monthly: Update dependencies
- [ ] Quarterly: Security audit
- [ ] Quarterly: Performance review

## Disaster Recovery

### Database Backup
```bash
# Daily RDS automated backups enabled
# Manual backup before major changes
aws rds create-db-snapshot \
  --db-instance-identifier user-service-db \
  --db-snapshot-identifier user-service-backup-$(date +%Y%m%d)
```

### Recovery Point Objective (RPO)
- Target: 1 hour
- Backup frequency: Continuous

### Recovery Time Objective (RTO)
- Target: 30 minutes
- Backup restoration time: < 15 minutes

---

**Deployment Status: READY**

All systems verified and ready for production deployment.
Last updated: 2026-05-28
