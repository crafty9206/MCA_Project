$ErrorActionPreference = 'Stop'
$root = 'C:\project\MCA_Project\Review2_Submission'
$out = Join-Path $root 'KARE_Abstract_Corrected_v2.pdf'
$editable = Join-Path $root 'KARE_Abstract_Corrected_v2.pptx'

function rgb([string]$hex) {
    $h = $hex.TrimStart('#')
    return ([Convert]::ToInt32($h.Substring(0, 2), 16) +
        ([Convert]::ToInt32($h.Substring(2, 2), 16) -shl 8) +
        ([Convert]::ToInt32($h.Substring(4, 2), 16) -shl 16))
}

function add-box($slide, [double]$x, [double]$y, [double]$w, [double]$h,
        [string]$fill, [int]$shapeType = 1, [string]$line = '') {
    $shape = $slide.Shapes.AddShape($shapeType, $x, $y, $w, $h)
    $shape.Fill.ForeColor.RGB = rgb $fill
    if ($line) { $shape.Line.ForeColor.RGB = rgb $line } else { $shape.Line.Visible = 0 }
    return $shape
}

function add-text($slide, [double]$x, [double]$y, [double]$w, [double]$h,
        [string]$value, [double]$size = 12, [bool]$bold = $false,
        [int]$alignment = 1, [double]$lineSpacingPoints = 12, [string]$color = '#111111') {
    $shape = $slide.Shapes.AddTextbox(1, $x, $y, $w, $h)
    $shape.TextFrame.WordWrap = -1
    $shape.TextFrame.MarginLeft = 0
    $shape.TextFrame.MarginRight = 0
    $shape.TextFrame.MarginTop = 0
    $shape.TextFrame.MarginBottom = 0
    $shape.TextFrame.AutoSize = 0
    $shape.TextFrame.AutoSize = 0
    $range = $shape.TextFrame.TextRange
    $range.Text = $value.Replace('\n', [Environment]::NewLine)
    $range.Font.Name = 'Times New Roman'
    $range.Font.Size = $size
    $range.Font.Bold = $bold
    $range.Font.Color.RGB = rgb $color
    $range.ParagraphFormat.Alignment = $alignment
    $range.ParagraphFormat.SpaceWithin = ($lineSpacingPoints / 12.0)
    $shape.Width = $w
    $shape.Height = $h
    return $shape
}

function add-entity($slide, [double]$x, [double]$y, [string]$name, [string]$fields) {
    add-box $slide $x $y 235 64 '#FFFFFF' 1 '#3F5A54' | Out-Null
    add-box $slide $x $y 235 23 '#DCEAE5' 1 '#3F5A54' | Out-Null
    add-text $slide ($x + 8) ($y + 3) 218 17 $name 10 $true 1 12 '#173B38' | Out-Null
    add-text $slide ($x + 8) ($y + 28) 218 31 $fields 9 $false 1 12 '#253D39' | Out-Null
}

$app = New-Object -ComObject PowerPoint.Application
$app.Visible = -1
$doc = $app.Presentations.Add()
$doc.PageSetup.SlideSize = 7
$doc.PageSetup.SlideWidth = 595
$doc.PageSetup.SlideHeight = 842

# Page 1: One-page abstract in the supplied CDOE format.
$slide = $doc.Slides.Add(1, 12)
$slide.FollowMasterBackground = 0
$slide.Background.Fill.ForeColor.RGB = rgb '#FFFFFF'
add-text $slide 32 19 405 22 'KALASALINGAM ACADEMY OF RESEARCH AND EDUCATION' 12 $true 1 14 '#164F88' | Out-Null
add-text $slide 32 42 405 18 '(DEEMED TO BE UNIVERSITY)' 9 $true 1 12 '#164F88' | Out-Null
add-text $slide 32 61 405 19 'CENTRE FOR DISTANCE AND ONLINE EDUCATION (CDOE)' 10 $true 1 13 '#B3262D' | Out-Null
add-box $slide 451 18 110 60 '#F6F8F7' 1 '#AABBB5' | Out-Null
add-text $slide 457 35 98 28 '[CDOE LOGO]' 8 $true 2 10 '#555555' | Out-Null

add-text $slide 42 107 505 22 'Name of the Learner: Deepak Kumar' 12 $true 1 14 | Out-Null
add-text $slide 310 107 238 22 'Register number: 9224116032' 12 $true 1 14 | Out-Null
add-text $slide 42 132 505 22 'Name of the Programme: MCA' 12 $true 1 14 | Out-Null
add-text $slide 310 132 238 22 'Semester: IV' 12 $true 1 14 | Out-Null
add-text $slide 42 157 505 22 'Name of the Course: Project Work' 12 $true 1 14 | Out-Null
add-text $slide 310 157 238 22 'Batch: Jan 2024' 12 $true 1 14 | Out-Null
add-text $slide 42 182 505 22 'Course Code: 223MCA4201' 12 $true 1 14 | Out-Null
add-text $slide 310 182 238 22 'Regulation: 2022' 12 $true 1 14 | Out-Null
add-text $slide 42 207 505 22 'Project Title: MaatriCare' 14 $true 1 16 | Out-Null
add-text $slide 42 237 505 22 'Abstract' 14 $true 2 18 '#111111' | Out-Null

$abstract = 'Pregnancy involves appointments, daily care, personal records, and communication with healthcare professionals. When information is scattered, organizing activities and preparing for visits can be difficult. MaatriCare is an authenticated web application that brings pregnancy tracking and care coordination into one workspace.\n\nThe system supports pregnancy profiles, appointments and visit questions, daily care and wellbeing tracking, a private symptom journal, milestones, educational resources, summaries, emergency contacts, and configurable reminders. Users choose which records to include in reports and may share selected reports through revocable, time-limited links. MaatriCare organizes user-entered information; it does not diagnose or recommend treatment and does not replace professional or emergency care.\n\nThe frontend uses React, TypeScript, and Vite; the backend uses Java Spring Boot REST APIs, Spring Security/JWT, validation, and JPA. MySQL is used locally and PostgreSQL is configured for production. Optional Web Push uses Asia/Kolkata time and requires VAPID keys and HTTPS for live delivery.'
add-text $slide 48 267 499 344 $abstract 12 $false 4 18 '#111111' | Out-Null

add-text $slide 42 625 505 20 'Tools & Technologies Used' 12 $true 1 14 '#111111' | Out-Null
add-text $slide 53 649 494 80 'Frontend: React, TypeScript, Vite\nBackend: Java, Spring Boot REST API\nDatabase: MySQL (local), PostgreSQL configuration\nSecurity: Spring Security, JWT' 12 $false 1 18 '#111111' | Out-Null
add-text $slide 42 737 505 20 'Approval Committee' 12 $true 2 14 '#111111' | Out-Null
add-box $slide 43 771 150 1 '#555555' | Out-Null
add-box $slide 223 771 150 1 '#555555' | Out-Null
add-box $slide 402 771 150 1 '#555555' | Out-Null
add-text $slide 40 778 156 26 'Internal Examiner' 9 $false 2 12 '#111111' | Out-Null
add-text $slide 214 778 170 26 'Deputy Director - CDOE' 9 $false 2 12 '#111111' | Out-Null
add-text $slide 399 778 156 26 'Director - CDOE' 9 $false 2 12 '#111111' | Out-Null

# Page 2: ER diagram only, with implemented user-owned tables.
$slide = $doc.Slides.Add(2, 12)
$slide.FollowMasterBackground = 0
$slide.Background.Fill.ForeColor.RGB = rgb '#FFFFFF'
add-text $slide 32 28 531 24 'ENTITY RELATIONSHIP DIAGRAM (ER)' 14 $true 2 18 '#111111' | Out-Null
add-text $slide 32 57 531 18 'MaatriCare | simplified implemented schema' 10 $false 2 12 '#444444' | Out-Null

# User parent and central relationship spine.
add-entity $slide 180 91 'users' 'id PK | email UNIQUE | timezone_id'
$spine = $slide.Shapes.AddConnector(1, 297.5, 155, 297.5, 659)
$spine.Line.ForeColor.RGB = rgb '#566E68'
$spine.Line.Weight = 1.5

$records = @(
    [pscustomobject]@{Name='pregnancy_profiles';Fields='id PK | user_id FK UNIQUE';Side='L'},
    [pscustomobject]@{Name='appointments';Fields='id PK | user_id FK | starts_at';Side='R'},
    [pscustomobject]@{Name='care_tasks';Fields='id PK | user_id FK | task_detail_id FK';Side='L'},
    [pscustomobject]@{Name='daily_wellbeing';Fields='user_id FK | entry_date UNIQUE';Side='R'},
    [pscustomobject]@{Name='symptom_entries';Fields='id PK | user_id FK | severity';Side='L'},
    [pscustomobject]@{Name='pregnancy_milestones';Fields='id PK | user_id FK | week_number';Side='R'},
    [pscustomobject]@{Name='emergency_contacts';Fields='id PK | user_id FK | type';Side='L'},
    [pscustomobject]@{Name='report_shares';Fields='id PK | user_id FK | expires_at';Side='R'},
    [pscustomobject]@{Name='push_subscriptions';Fields='id PK | user_id FK | endpoint_hash';Side='L'},
    [pscustomobject]@{Name='push_reminders';Fields='id PK | user_id FK | scheduled_at';Side='R'}
)
for ($i = 0; $i -lt $records.Count; $i++) {
    $record = $records[$i]
    $row = [Math]::Floor($i / 2)
    $y = 174.0 + ($row * 93.0)
    if ($record.Side -eq 'L') {
        $x = 31.0
        $link = $slide.Shapes.AddConnector(1, 266, ($y + 32), 297.5, ($y + 32))
    } else {
        $x = 329.0
        $link = $slide.Shapes.AddConnector(1, 297.5, ($y + 32), 329, ($y + 32))
    }
    $link.Line.ForeColor.RGB = rgb '#566E68'
    $link.Line.Weight = 1
    add-entity $slide $x $y $record.Name $record.Fields
}

add-text $slide 35 661 525 62 'Relationships: one user owns one pregnancy profile and may own many appointments, tasks, wellbeing records, symptoms, milestones, shares, subscriptions, and reminders. Appointment questions reference appointments; care tasks reference task details.' 9 $false 1 12 '#333333' | Out-Null
Get-Item $editable, $out | Select-Object Name, Length, LastWriteTime
