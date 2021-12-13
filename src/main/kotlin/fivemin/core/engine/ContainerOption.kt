package fivemin.core.engine

data class ContainerOption(val workingSetMode: WorkingSetMode) {
}

enum class WorkingSetMode{
    Enabled, Disabled
}