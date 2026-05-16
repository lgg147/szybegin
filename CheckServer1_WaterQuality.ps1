# CheckServer1_WaterQuality.ps1
# 服务器1诊断脚本 - 检查是否向服务器2的eb_database写入水质数据

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务器1水质数据采集诊断" -ForegroundColor Cyan
Write-Host "  执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. 检查collector和msgclient进程状态
Write-Host "[1/6] 检查采集程序进程..." -ForegroundColor Yellow
$collectorProc = Get-Process | Where-Object {$_.Path -like "*collector.exe"}
$msgclientProc = Get-Process | Where-Object {$_.Path -like "*msgclient.exe"}

if ($collectorProc) {
    Write-Host "  ✓ collector.exe 正在运行 (PID: $($collectorProc.Id))" -ForegroundColor Green
    Write-Host "    启动时间: $($collectorProc.StartTime)"
} else {
    Write-Host "  ✗ collector.exe 未运行" -ForegroundColor Red
}

if ($msgclientProc) {
    Write-Host "  ✓ msgclient.exe 正在运行 (PID: $($msgclientProc.Id))" -ForegroundColor Green
    Write-Host "    启动时间: $($msgclientProc.StartTime)"
} else {
    Write-Host "  ✗ msgclient.exe 未运行" -ForegroundColor Red
}

# 2. 检查collector配置文件中的数据库配置
Write-Host "`n[2/6] 检查collector数据库配置..." -ForegroundColor Yellow
$configFile = "D:\collector\collector\config\application-prod.yml"
if (Test-Path $configFile) {
    Write-Host "  配置文件: $configFile" -ForegroundColor Green
    $configContent = Get-Content $configFile
    
    # 查找所有数据库配置
    $dbConfigs = $configContent | Select-String -Pattern "jdbc:|url:|username:|password:" -Context 1,0
    
    if ($dbConfigs) {
        Write-Host "  发现的数据库配置:" -ForegroundColor Cyan
        $dbConfigs | ForEach-Object {
            Write-Host "    $($_.Line)"
        }
        
        # 特别检查是否有eb_database的配置
        $ebDbConfig = $configContent | Select-String -Pattern "eb_database" -CaseSensitive:$false
        if ($ebDbConfig) {
            Write-Host "  ✓ 发现eb_database相关配置" -ForegroundColor Green
            $ebDbConfig | ForEach-Object { Write-Host "    $($_.Line)" }
        } else {
            Write-Host "  ✗ 未发现eb_database配置（可能由其他程序负责）" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  ✗ 配置文件不存在: $configFile" -ForegroundColor Red
}

# 3. 检查msgclient日志中是否有eb_database或bb表的写入记录
Write-Host "`n[3/6] 检查msgclient日志..." -ForegroundColor Yellow
$msgclientLog = "D:\collector\collector\consumer_logs\info.log"
if (Test-Path $msgclientLog) {
    Write-Host "  日志文件: $msgclientLog" -ForegroundColor Green
    
    # 搜索最近1000行中包含eb_database、bb表、水质的记录
    $waterQualityLogs = Get-Content $msgclientLog -Tail 1000 | 
    Select-String -Pattern "eb_database|\.bb|水质|quality|PH|YL|DDL" -CaseSensitive:$false
    
    if ($waterQualityLogs) {
        Write-Host "  ✓ 发现 $($waterQualityLogs.Count) 条水质相关日志" -ForegroundColor Green
        Write-Host "  最近5条:" -ForegroundColor Cyan
        $waterQualityLogs | Select-Object -Last 5 | ForEach-Object {
            Write-Host "    $($_.Line)"
        }
    } else {
        Write-Host "  ✗ 未发现水质相关日志（msgclient可能不处理水质数据）" -ForegroundColor Yellow
    }
    
    # 检查是否有INSERT语句
    $insertLogs = Get-Content $msgclientLog -Tail 500 | 
    Select-String -Pattern "INSERT INTO.*bb|INSERT INTO.*eb_database" -CaseSensitive:$false
    
    if ($insertLogs) {
        Write-Host "  ✓ 发现bb表INSERT操作" -ForegroundColor Green
        $insertLogs | Select-Object -Last 3 | ForEach-Object {
            Write-Host "    $($_.Line)"
        }
    } else {
        Write-Host "  ✗ 未发现bb表INSERT操作" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✗ 日志文件不存在: $msgclientLog" -ForegroundColor Red
}

# 4. 检查collector日志中是否有水质相关的TCP数据接收
Write-Host "`n[4/6] 检查collector原始数据接收日志..." -ForegroundColor Yellow
$collectorLog = "D:\collector\collector\logs\info.log"
if (Test-Path $collectorLog) {
    Write-Host "  日志文件: $collectorLog" -ForegroundColor Green
    
    # 搜索水质关键词
    $waterLogs = Get-Content $collectorLog -Tail 1000 | 
    Select-String -Pattern "PH|YL|DDL|水质|quality|sensor" -CaseSensitive:$false
    
    if ($waterLogs) {
        Write-Host "  ✓ 发现 $($waterLogs.Count) 条水质相关日志" -ForegroundColor Green
        Write-Host "  最近5条:" -ForegroundColor Cyan
        $waterLogs | Select-Object -Last 5 | ForEach-Object {
            Write-Host "    $($_.Line)"
        }
    } else {
        Write-Host "  ✗ 未发现水质相关日志（RTU可能未发送水质数据）" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✗ 日志文件不存在: $collectorLog" -ForegroundColor Red
}

# 5. 检查网络连接 - 是否有连接到服务器2的MySQL端口
Write-Host "`n[5/6] 检查与服务器2的网络连接..." -ForegroundColor Yellow
Write-Host "  提示: 请提供服务器2的IP地址以检查连接状态" -ForegroundColor Cyan
Write-Host "  当前所有MySQL连接（端口3306）:" -ForegroundColor Cyan

$mysqlConnections = Get-NetTCPConnection | Where-Object {
    $_.RemotePort -eq 3306 -and $_.State -eq "Established"
}

if ($mysqlConnections) {
    $mysqlConnections | ForEach-Object {
        Write-Host "  连接到: $($_.RemoteAddress):$($_.RemotePort)" -ForegroundColor Green
        $proc = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "    进程: $($proc.ProcessName) (PID: $($proc.Id))"
        }
    }
} else {
    Write-Host "  ✗ 未发现活跃的MySQL连接" -ForegroundColor Yellow
}

# 6. 总结和建议
Write-Host "`n[6/6] 诊断总结" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$issues = @()

if (-not $collectorProc) {
    $issues += "collector.exe 未运行"
}
if (-not $msgclientProc) {
    $issues += "msgclient.exe 未运行"
}

$configContent = Get-Content $configFile -ErrorAction SilentlyContinue
if ($configContent -notmatch "eb_database") {
    $issues += "配置文件中未发现eb_database配置"
}

$msgclientLogContent = Get-Content $msgclientLog -Tail 1000 -ErrorAction SilentlyContinue
if ($msgclientLogContent -notmatch "eb_database|\.bb|水质") {
    $issues += "msgclient日志中无水质数据处理记录"
}

if ($issues.Count -eq 0) {
    Write-Host "✓ 服务器1的采集程序运行正常" -ForegroundColor Green
    Write-Host "`n重要提示:" -ForegroundColor Cyan
    Write-Host "  服务器1可能不负责水质数据采集" -ForegroundColor Yellow
    Write-Host "  水质数据可能由服务器2上的独立采集程序负责" -ForegroundColor Yellow
    Write-Host "  请在服务器2上执行 CheckServer2_WaterQuality.ps1 脚本" -ForegroundColor Yellow
} else {
    Write-Host "✗ 发现以下问题:" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "  - $issue" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "诊断完成，请将此结果提供给技术人员" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
