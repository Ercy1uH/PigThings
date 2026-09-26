<#
    PigThings - 一键打包三个版本，jar 按版本收集到 .\dist\<Minecraft版本>

    用法（在仓库根目录执行）：
        powershell -ExecutionPolicy Bypass -File .\build-all.ps1
        powershell -ExecutionPolicy Bypass -File .\build-all.ps1 -Targets 1.20.1
        powershell -ExecutionPolicy Bypass -File .\build-all.ps1 -SkipChecks

    Forge 1.20.1 和 NeoForge 1.21.1 使用 Gradle 8.8，Forge 1.12.2 使用 Gradle 4.9。
    自动检测 JDK 17、21、8；构建产物按版本收集到 dist。
#>
[CmdletBinding()]
param(
    [ValidateSet('1.20.1', '1.21.1', '1.12.2', 'all')]
    [string[]]$Targets = @('all'),
    # 只出 jar，跳过 check/test
    [switch]$SkipChecks,
    # 受限环境（默认 ~/.gradle 不可写）时，把 Gradle 缓存指到别处，例如 -GradleUserHome .\.gradle-home
    [string]$GradleUserHome = ''
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$dist = Join-Path $root 'dist'

if ($GradleUserHome) {
    $resolved = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($GradleUserHome)
    New-Item -ItemType Directory -Force -Path $resolved | Out-Null
    $env:GRADLE_USER_HOME = $resolved
    Write-Host ('GRADLE_USER_HOME -> ' + $resolved) -ForegroundColor Yellow
}

function Find-Jdk {
    param([Parameter(Mandatory = $true)][string]$Pattern)
    $searchRoots = @(
        'C:\Program Files\Java',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Zulu',
        'C:\Program Files\BellSoft'
    )
    foreach ($r in $searchRoots) {
        if (-not (Test-Path -LiteralPath $r)) { continue }
        $hit = Get-ChildItem -LiteralPath $r -Directory -ErrorAction SilentlyContinue |
               Where-Object { $_.Name -like $Pattern } |
               Sort-Object -Property Name -Descending |
               Select-Object -First 1
        if ($hit -and (Test-Path -LiteralPath (Join-Path $hit.FullName 'bin\java.exe'))) {
            return $hit.FullName
        }
    }
    return $null
}

function Invoke-OneBuild {
    param(
        [Parameter(Mandatory = $true)][System.Collections.IDictionary]$Build,
        [Parameter(Mandatory = $true)][string]$Jdk,
        [Parameter(Mandatory = $true)][string]$Task
    )
    Write-Host ''
    Write-Host ('=== {0}   JDK: {1} ===' -f $Build.Name, $Jdk) -ForegroundColor Cyan
    $gradlew = Join-Path $Build.Dir 'gradlew.bat'
    if (-not (Test-Path -LiteralPath $gradlew)) { throw ('找不到 ' + $gradlew) }

    # Select the JDK for the daemon and Java toolchains.
    $overrideJavaHome = '-Dorg.gradle.java.home=' + $Jdk
    $overrideInstall  = '-Porg.gradle.java.installations.paths=' + $Jdk

    $savedJavaHome = $env:JAVA_HOME
    $env:JAVA_HOME = $Jdk
    Push-Location -LiteralPath $Build.Dir
    try {
        & $gradlew $Task $overrideJavaHome $overrideInstall
        if ($LASTEXITCODE -ne 0) {
            throw ('{0} 构建失败，退出码 {1}。常见原因：Gradle 发行版/依赖下载失败（需要能访问 ' +
                   'services.gradle.org、maven.minecraftforge.net、maven.neoforged.net），' +
                   '或 ~/.gradle 不可写（可试 -GradleUserHome）。' -f $Build.Name, $LASTEXITCODE)
        }
    }
    finally {
        Pop-Location
        $env:JAVA_HOME = $savedJavaHome
    }
}

function Copy-Artifacts {
    param([Parameter(Mandatory = $true)][System.Collections.IDictionary]$Build)
    $properties = Get-Content -Raw -LiteralPath (Join-Path $Build.Dir 'gradle.properties') | ConvertFrom-StringData
    $loader = if ($properties.ContainsKey('forge_version')) { '-forge' } else { '' }
    $artifactName = '{0}-{1}{2}-{3}.jar' -f $properties.mod_id, $properties.mod_version, $loader, $properties.minecraft_version
    $found = @()
    foreach ($dir in $Build.Collect) {
        $artifact = Join-Path $dir $artifactName
        if (Test-Path -LiteralPath $artifact -PathType Leaf) {
            $found += Get-Item -LiteralPath $artifact
        }
    }
    if ($found.Count -eq 0) {
        throw ('{0}: 没找到当前版本产物 {1}（查找目录: {2}）' -f $Build.Name, $artifactName, ($Build.Collect -join '; '))
    }
    $copied = @()
    $versionDist = Join-Path $dist $Build.Key
    New-Item -ItemType Directory -Force -Path $versionDist | Out-Null
    foreach ($jar in $found) {
        $dest = Join-Path $versionDist $jar.Name
        Copy-Item -LiteralPath $jar.FullName -Destination $dest -Force
        $copied += $dest
    }
    return $copied
}

$allBuilds = @(
    [ordered]@{
        Key     = '1.20.1'
        Name    = 'Forge 1.20.1'
        Dir     = (Join-Path $root '1.20.1')
        Jdk     = 'jdk-17*'
        Collect = @((Join-Path $root '1.20.1\build\libs'))
    },
    [ordered]@{
        Key     = '1.21.1'
        Name    = 'NeoForge 1.21.1'
        Dir     = (Join-Path $root '1.21.1')
        Jdk     = 'jdk-21*'
        Collect = @((Join-Path $root '1.21.1\build\libs'))
    },
    [ordered]@{
        Key     = '1.12.2'
        Name    = 'Forge 1.12.2'
        Dir     = (Join-Path $root '1.12.2')
        Jdk     = 'jdk1.8*'
        Collect = @((Join-Path $root '1.12.2\build\libs'))
    }
)

$wanted = $allBuilds
if ($Targets -notcontains 'all') {
    $wanted = @($allBuilds | Where-Object { $Targets -contains $_.Key })
}

New-Item -ItemType Directory -Force -Path $dist | Out-Null

$task = 'build'
if ($SkipChecks) { $task = 'assemble' }

$problems = @()
foreach ($b in $wanted) {
    $jdk = Find-Jdk -Pattern $b.Jdk
    if (-not $jdk) {
        $msg = '{0}: 没找到匹配 {1} 的 JDK' -f $b.Name, $b.Jdk
        $problems += $msg
        Write-Warning $msg
        continue
    }
    Invoke-OneBuild -Build $b -Jdk $jdk -Task $task
    foreach ($j in (Copy-Artifacts -Build $b)) {
        Write-Host ('  -> ' + $j) -ForegroundColor Green
    }
}

Write-Host ''
Write-Host ('产物目录: ' + $dist) -ForegroundColor Cyan
Get-ChildItem -LiteralPath $dist -Filter '*.jar' -File -Recurse -ErrorAction SilentlyContinue |
    Sort-Object FullName |
    ForEach-Object { Write-Host ('  {0,10}  {1}' -f $_.Length, $_.FullName.Substring($dist.Length + 1)) }

if ($problems.Count -gt 0) {
    Write-Host ''
    Write-Warning ('有 {0} 个目标未构建:' -f $problems.Count)
    foreach ($p in $problems) { Write-Warning ('  ' + $p) }
    exit 1
}
