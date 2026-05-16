# CheckServer2_WaterQuality.ps1
# 服务器2诊断脚本 - 查找水质数据采集程序

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务器2水质数据采集程序诊断" -ForegroundColor Cyan
Write-Host "  执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. 检查bb表的最新数据状态
Write-Host "[1/8] 检查eb_database.bb表数据状态..." -ForegroundColor Yellow
Write-Host "  请在MySQL中执行以下SQL并查看结果:" -ForegroundColor Cyan
Write-Host "  SELECT MAX(dateTime), COUNT(*) FROM eb_database.bb WHERE dateTime >= DATE_SUB(NOW(), INTERVAL 2 HOUR);" -ForegroundColor White
Write-Host ""

# 尝试通过命令行执行SQL（如果mysql.exe在PATH中）
try {
    $result = & mysql -u root -p -h localhost -e "SELECT MAX(dateTime) as latest_time, COUNT(*) as count FROM eb_database.bb WHERE dateTime >= DATE_SUB(NOW(), INTERVAL 2 HOUR);" 2>&1
    if ($result) {
        Write-Host "  SQL执行结果:" -ForegroundColor Green
        $result | ForEach-Object { Write-Host "    $_" }
    }
} catch {
    Write-Host "  ⚠ 无法自动执行SQL，请手动在MySQL客户端执行" -ForegroundColor Yellow
}

# 2. 查找所有Java进程
Write-Host "`n[2/8] 查找Java进程（采集程序通常是Java应用）..." -ForegroundColor Yellow
$javaProcs = Get-Process | Where-Object {$_.ProcessName -eq "java"}

if ($javaProcs) {
    Write-Host "  ✓ 发现 $($javaProcs.Count) 个Java进程" -ForegroundColor Green
    foreach ($proc in $javaProcs) {
        Write-Host "`n  --- Java进程 PID: $($proc.Id) ---" -ForegroundColor Cyan
        Write-Host "  启动时间: $($proc.StartTime)"
        Write-Host "  CPU使用: $($proc.CPU)"
        Write-Host "  内存使用: $([math]::Round($proc.WorkingSet / 1MB, 2)) MB"
        
        # 尝试获取JVM启动参数
        try {
            $wmic = Get-WmiObject Win32_Process -Filter "ProcessId = $($proc.Id)"
            if ($wmic.CommandLine) {
                Write-Host "  启动命令:" -ForegroundColor Yellow
                # 分行显示长命令
                $wmic.CommandLine -split " " | ForEach-Object {
                    Write-Host "    $_"
                }
                
                # 检查是否包含collector、water、quality等关键词
                if ($wmic.CommandLine -match "collector|water|quality|bb|水质") {
                    Write-Host "  ✓ 此进程可能是水质采集程序" -ForegroundColor Green
                }
            }
        } catch {
            Write-Host "  ⚠ 无法获取启动参数（可能需要管理员权限）" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  ✗ 未发现Java进程" -ForegroundColor Red
}

# 3. 查找其他可能的采集程序进程
Write-Host "`n[3/8] 查找其他可能的采集程序..." -ForegroundColor Yellow
$keywords = @("collector", "water", "quality", "monitor", "sensor", "采集", "rtu")
foreach ($keyword in $keywords) {
    $procs = Get-Process | Where-Object {
        $_.ProcessName -like "*$keyword*" -or 
        $_.Path -like "*$keyword*"
    }
    if ($procs) {
        Write-Host "  ✓ 发现包含 '$keyword' 的进程:" -ForegroundColor Green
        $procs | ForEach-Object {
            Write-Host "    - $($_.ProcessName) (PID: $($_.Id))"
            Write-Host "      路径: $($_.Path)"
        }
    }
}

# 4. 查找Windows服务
Write-Host "`n[4/8] 查找Windows服务..." -ForegroundColor Yellow
$services = Get-Service | Where-Object {
    $_.DisplayName -match "采集|collector|water|quality|monitor|bb|水质|RTU" -or
    $_.Name -match "collector|water|quality|monitor"
}

if ($services) {
    Write-Host "  ✓ 发现相关服务:" -ForegroundColor Green
    $services | ForEach-Object {
        $statusColor = if ($_.Status -eq "Running") { "Green" } else { "Red" }
        Write-Host "  - $($_.DisplayName)" -ForegroundColor $statusColor
        Write-Host "    状态: $($_.Status) | 启动类型: $($_.StartType)"
        
        # 获取服务的可执行文件路径
        try {
            $wmiService = Get-WmiObject Win32_Service -Filter "Name='$($_.Name)'"
            if ($wmiService.PathName) {
                Write-Host "    路径: $($wmiService.PathName)"
            }
        } catch {}
    }
} else {
    Write-Host "  ✗ 未发现相关的Windows服务" -ForegroundColor Yellow
}

# 5. 查找计划任务
Write-Host "`n[5/8] 查找Windows计划任务..." -ForegroundColor Yellow
$tasks = Get-ScheduledTask | Where-Object {
    $_.TaskName -match "采集|collector|water|quality|bb|同步|sync"
}

if ($tasks) {
    Write-Host "  ✓ 发现相关计划任务:" -ForegroundColor Green
    $tasks | ForEach-Object {
        $taskInfo = Get-ScheduledTaskInfo -TaskName $_.TaskName
        Write-Host "  - $($_.TaskName)" -ForegroundColor Cyan
        Write-Host "    状态: $($taskInfo.State)"
        Write-Host "    最后运行: $($taskInfo.LastRunTime)"
        Write-Host "    下次运行: $($taskInfo.NextRunTime)"
    }
} else {
    Write-Host "  ✗ 未发现相关的计划任务" -ForegroundColor Yellow
}

# 6. 查找日志文件
Write-Host "`n[6/8] 查找水质相关日志文件..." -ForegroundColor Yellow
$logFiles = Get-ChildItem -Path "C:\", "D:\", "E:\" -Filter "*.log" -Recurse -ErrorAction SilentlyContinue | 
Where-Object {$_.LastWriteTime -gt (Get-Date).AddDays(-7)} |
Select-Object -First 50

$matchedLogs = @()
foreach ($logFile in $logFiles) {
    try {
        $content = Get-Content $logFile.FullName -Tail 100 -ErrorAction SilentlyContinue
        if ($content | Select-String -Pattern "bb|PH|YL|DDL|水质|INSERT INTO.*bb" -CaseSensitive:$false -Quiet) {
            $matchedLogs += $logFile
        }
    } catch {}
}

if ($matchedLogs.Count -gt 0) {
    Write-Host "  ✓ 发现 $($matchedLogs.Count) 个包含水质信息的日志文件:" -ForegroundColor Green
    $matchedLogs | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $($_.FullName)" -ForegroundColor Cyan
        Write-Host "    最后修改: $($_.LastWriteTime)"
        Write-Host "    文件大小: $([math]::Round($_.Length / 1KB, 2)) KB"
        
        # 显示最后5行
        Write-Host "    最新日志内容:" -ForegroundColor Yellow
        Get-Content $_.FullName -Tail 5 | ForEach-Object {
            Write-Host "      $_"
        }
        Write-Host ""
    }
} else {
    Write-Host "  ✗ 未发现包含水质信息的日志文件" -ForegroundColor Yellow
}

# 7. 查找监听端口
Write-Host "`n[7/8] 查找监听的端口（可能接收RTU数据）..." -ForegroundColor Yellow
$listeningPorts = Get-NetTCPConnection | Where-Object {$_.State -eq "Listen"} | 
Where-Object {$_.LocalPort -lt 10000} |
Select-Object LocalPort, OwningProcess |
Sort-Object LocalPort

if ($listeningPorts) {
    Write-Host "  ✓ 发现以下监听端口:" -ForegroundColor Green
    $listeningPorts | ForEach-Object {
        $proc = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        $portColor = if ($_.LocalPort -in @(8086, 8080, 9000, 5000, 4001)) { "Green" } else { "White" }
        Write-Host "  - 端口 $($_.LocalPort)" -ForegroundColor $portColor
        if ($proc) {
            Write-Host "    进程: $($proc.ProcessName) (PID: $($proc.Id))"
            Write-Host "    路径: $($proc.Path)"
        }
    }
} else {
    Write-Host "  ✗ 未发现监听端口" -ForegroundColor Yellow
}

# 8. 检查MySQL进程列表
Write-Host "`n[8/8] 检查MySQL连接情况..." -ForegroundColor Yellow
Write-Host "  请在MySQL中执行: SHOW PROCESSLIST;" -ForegroundColor Cyan
Write-Host "  查看是否有持续的INSERT操作写入bb表" -ForegroundColor Cyan

try {
    $processList = & mysql -u root -p -h localhost -e "SHOW PROCESSLIST;" 2>&1
    if ($processList) {
        Write-Host "  MySQL进程列表:" -ForegroundColor Green
        $processList | ForEach-Object { Write-Host "    $_" }
        
        # 检查是否有INSERT操作
        if ($processList | Select-String -Pattern "INSERT.*bb|INSERT.*eb_database" -CaseSensitive:$false) {
            Write-Host "  ✓ 发现正在执行的INSERT操作" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 未发现INSERT操作" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "  ⚠ 无法自动查询MySQL，请手动执行 SHOW PROCESSLIST;" -ForegroundColor Yellow
}

# 总结
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  诊断总结与建议" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n根据以上检查结果，判断水质采集程序的状态:" -ForegroundColor Yellow
Write-Host ""
Write-Host "情况1: 如果发现Java进程且日志中有bb表写入记录" -ForegroundColor Cyan
Write-Host "  → 采集程序正常运行，问题可能在RTU设备或传感器" -ForegroundColor White
Write-Host ""
Write-Host "情况2: 如果未发现任何采集程序" -ForegroundColor Cyan
Write-Host "  → 采集程序可能未启动或已崩溃" -ForegroundColor White
Write-Host "  → 检查Windows事件日志: Get-EventLog -LogName Application -After (Get-Date).AddDays(-7)" -ForegroundColor White
Write-Host ""
Write-Host "情况3: 如果bb表长时间无新数据" -ForegroundColor Cyan
Write-Host "  → RTU设备可能未发送水质数据" -ForegroundColor White
Write-Host "  → 需要现场检查水质传感器和RTU配置" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "请将此诊断结果提供给技术人员进行进一步分析" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
