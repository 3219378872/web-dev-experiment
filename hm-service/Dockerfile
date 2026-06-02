# 基础镜像（对齐其它微服务，openjdk:11.0-jre-buster tag 已从 registry 移除）
FROM eclipse-temurin:11-jre
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 拷贝jar包
COPY target/*.jar app.jar
# 入口
ENTRYPOINT ["java", "-jar", "/app.jar"]
