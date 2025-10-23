# Security Guidelines

## 🔒 API Key Management

This project requires several API keys to function properly. **NEVER** commit API keys or sensitive credentials to version control.

## 📝 Required API Keys

### 1. Google Gemini API Key
- **Purpose**: AI-powered virtual try-on functionality
- **Get it from**: https://makersuite.google.com/app/apikey
- **Documentation**: https://ai.google.dev/

### 2. AWS Credentials
- **Purpose**: Image storage and retrieval via S3
- **Get them from**: https://console.aws.amazon.com/iam/
- **Required**:
  - AWS Access Key ID
  - AWS Secret Access Key
  - S3 Bucket Name

## ⚙️ Configuration Setup

### Backend Configuration

1. **Copy the example file**:
   ```bash
   cd backend/src/main/resources
   cp application-local.yml.example application-local.yml
   ```

2. **Edit `application-local.yml`** and replace placeholders:
   - `YOUR_GOOGLE_GEMINI_API_KEY_HERE` → your actual Gemini API key
   - `YOUR_AWS_ACCESS_KEY_HERE` → your AWS access key
   - `YOUR_AWS_SECRET_KEY_HERE` → your AWS secret key

3. **Alternative: Use Environment Variables**:
   ```bash
   export GOOGLE_GEMINI_API_KEY="your_key_here"
   export AWS_ACCESS_KEY_ID="your_access_key_here"
   export AWS_SECRET_ACCESS_KEY="your_secret_key_here"
   ```

### Environment Variables

You can also copy `.env.example` to `.env` and set your values there:

```bash
cp .env.example .env
# Edit .env with your actual values
```

## 🚫 What NOT to Commit

The following files are in `.gitignore` and should **NEVER** be committed:

- `application-local.yml`
- `application-dev.yml` (if it contains real credentials)
- `application-prod.yml`
- `.env`
- `.env.local`
- Any file containing actual API keys

## ✅ What IS Safe to Commit

- `application.yml` (with environment variable placeholders only)
- `application-local.yml.example`
- `application-dev.yml.example`
- `.env.example`

## 🔍 Checking for Exposed Secrets

Before committing, always verify you haven't accidentally included secrets:

```bash
# Search for potential API keys in staged files
git diff --cached | grep -i "api[-_]key"
git diff --cached | grep -i "secret"
```

## 🛡️ If You Accidentally Commit Secrets

1. **Immediately rotate/revoke the exposed credentials**:
   - Google Gemini: https://makersuite.google.com/app/apikey
   - AWS: https://console.aws.amazon.com/iam/

2. **Remove from Git history**:
   ```bash
   # For the most recent commit
   git reset --soft HEAD~1
   git reset HEAD application-local.yml
   git commit -c ORIG_HEAD
   
   # For older commits, use git filter-branch or BFG Repo-Cleaner
   ```

3. **Force push** (if already pushed to remote):
   ```bash
   git push --force-with-lease
   ```

## 📚 Best Practices

1. ✅ Always use environment variables or config files in `.gitignore`
2. ✅ Use different credentials for development/staging/production
3. ✅ Regularly rotate API keys
4. ✅ Use AWS IAM roles with minimal required permissions
5. ✅ Review `.gitignore` before adding new config files
6. ✅ Use tools like `git-secrets` to prevent accidental commits

## 🆘 Support

If you have questions about security setup, please contact the development team.

---

**Remember: When in doubt, DON'T commit! Ask first.**

