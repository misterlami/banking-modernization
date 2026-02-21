FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY BankingSystem1 ./BankingSystem1

RUN mkdir -p /workspace/build/WEB-INF/classes \
    && cp -r /workspace/BankingSystem1/* /workspace/build/ \
    && find /workspace/build -name "*.java" -not -path "*/WEB-INF/classes/*" -delete \
    && javac -cp "/workspace/BankingSystem1/WEB-INF/lib/*" -d /workspace/build/WEB-INF/classes $(find /workspace/BankingSystem1 -name "*.java") \
    && cd /workspace/build \
    && jar -cf /workspace/banking.war .

FROM tomcat:10.1-jdk17-temurin

COPY --from=builder /workspace/banking.war /usr/local/tomcat/webapps/banking.war

EXPOSE 8080
