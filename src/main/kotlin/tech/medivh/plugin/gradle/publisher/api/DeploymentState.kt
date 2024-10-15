package tech.medivh.plugin.gradle.publisher.api


enum class DeploymentState {
    PENDING, VALIDATING, VALIDATED,PUBLISHING, PUBLISHED, FAILED
}
