$ErrorActionPreference = 'Stop'
$root = 'C:\project\MCA_Project\Review2_Submission'
$out = Join-Path $root 'KARE_Abstract_Final_Review2.pdf'
$editable = Join-Path $root 'KARE_Abstract_Final_Review2.pptx'

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
        [int]$alignment = 1, [double]$lineSpacing = 1.0, [string]$color = '#111111') {
    $shape = $slide.Shapes.AddTextbox(1, $x, $y, $w, $h)
    $shape.TextFrame.WordWrap = -1
    $shape.TextFrame.MarginLeft = 0
    $shape.TextFrame.MarginRight = 0
    $shape.TextFrame.MarginTop = 0
    $shape.TextFrame.MarginBottom = 0
    $range = $shape.TextFrame.TextRange
    $range.Text = $value.Replace('\n', [Environment]::NewLine)
    $range.Font.Name = 'Times New Roman'
    $range.Font.Size = $size
    $range.Font.Bold = $bold
    $range.Font.Color.RGB = rgb $color
    $range.ParagraphFormat.Alignment = $alignment
    $range.ParagraphFormat.SpaceWithin = $lineSpacing
    return $shape
}

function add-entity($slide, [double]$x, [double]$y, [string]$name, [string]$fields) {
    add-box $slide $x $y 235 62 '#FFFFFF' 1 '#4F6F68' | Out-Null
    add-box $slide $x $y 235 22 '#DCEAE5' 1 '#4F6F68' | Out-Null
    add-text $slide ($x + 7) ($y + 3) 220 16 $name 10 $true 1 1 '#173B38' | Out-Null
    add-text $slide ($x + 7) ($y + 27) 220 31 $fields 8 $false 1 1 '#253D39' | Out-Null
}

$app = New-Object -ComObject PowerPoint.Application
$app.Visible = -1
$doc = $app.Presentations.Add()
$doc.PageSetup.SlideSize = 7
$doc.PageSetup.SlideWidth = 595
$doc.PageSetup.SlideHeight = 842

# Page 1: institutional abstract format.
$slide = $doc.Slides.Add(1, 12)
$slide.FollowMasterBackground = 0
$slide.Background.Fill.ForeColor.RGB = rgb '#FFFFFF'
add-text $slide 34 22 527 22 'KALASALINGAM ACADEMY OF RESEARCH AND EDUCATION' 14 $true 2 1 '#164F88' | Out-Null
add-text $slide 34 45 527 18 '(DEEMED TO BE UNIVERSITY)' 10 $true 2 1 '#164F88' | Out-Null
add-text $slide 34 63 527 18 'CENTRE FOR DISTANCE AND ONLINE EDUCATION (CDOE)' 11 $true 2 1 '#B3262D' | Out-Null
add-text $slide 36 82 523 18 '[Official college logo to be inserted]' 8 $false 2 1 '#666666' | Out-Null

add-text $slide 45 117 505 26 'Name of the Learner: Deepak Kumar' 12 $true 1 1 | Out-Null
add-text $slide 340 117 215 26 'Register number: 9224116032' 12 $true 1 1 | Out-Null
add-text $slide 45 146 505 23 'Name of the Programme: MCA' 12 $true 1 1 | Out-Null
add-text $slide 340 146 215 23 'Semester: IV' 12 $true 1 1 | Out-Null
add-text $slide 45 174 505 23 'Name of the Course: Project Work' 12 $true 1 1 | Out-Null
add-text $slide 340 174 215 23 'Batch: Jan 2024' 12 $true 1 1 | Out-Null
add-text $slide 45 202 505 23 'Course Code: 223MCA4201' 12 $true 1 1 | Out-Null
add-text $slide 340 202 215 23 'Regulation: 2022' 12 $true 1 1 | Out-Null
add-text $slide 45 230 505 24 'Project Title: MaatriCare' 12 $true 1 1 | Out-Null

add-text $slide 45 263 505 22 'Abstract' 14 $true 2 1 '#111111' | Out-Null
$abstract = 'Pregnancy involves regular appointments, daily care, personal records, and communication with healthcare professionals. When information is scattered across separate tools, organizing tasks and preparing for visits can be difficult. MaatriCare is a pregnancy support and care-coordination application that brings these activities into one authenticated workspace.\n\nThe system supports pregnancy profiles, appointments and visit questions, daily care and wellbeing tracking, a private symptom journal, milestones, educational resources, healthcare summaries, emergency contacts, and configurable reminders. Users choose which records to include in reports and may share selected reports through revocable, time-limited links. MaatriCare organizes user-entered information; it does not diagnose, recommend treatment, or replace professional or emergency care.\n\nThe frontend uses React, TypeScript, and Vite. The backend uses Java Spring Boot REST APIs, Spring Security/JWT, validation, and JPA. MySQL is used locally and PostgreSQL is configured for production. Optional scheduled Web Push uses Asia/Kolkata time and requires VAPID keys and HTTPS for live delivery.'
add-text $slide 48 296 500 318 $abstract 12 $false 4 1.5 '#111111' | Out-Null
add-text $slide 45 631 505 21 'Tools & Technologies Used' 12 $true 1 1 | Out-Null
add-text $slide 55 654 495 68 'Frontend: React, TypeScript, Vite\nBackend: Java, Spring Boot REST API\nDatabase: MySQL (local), PostgreSQL configuration\nSecurity: Spring Security, JWT' 11 $false 1 1.5 '#111111' | Out-Null
add-text $slide 38 746 520 20 'Approval Committee' 12 $true 2 1 | Out-Null
add-text $slide 35 778 170 28 'Internal Examiner' 10 $false 2 1 | Out-Null
add-text $slide 212 778 185 28 'Deputy Director - CDOE' 10 $false 2 1 | Out-Null
add-text $slide 402 778 155 28 'Director - CDOE' 10 $false 2 1 | Out-Null

# Page 2: one-page ER diagram for implemented relational records.
$slide = $doc.Slides.Add(2, 12)
$slide.FollowMasterBackground = 0
$slide.Background.Fill.ForeColor.RGB = rgb '#FFFFFF'
add-text $slide 34 24 527 26 'ENTITY RELATIONSHIP DIAGRAM (ER)' 14 $true 2 1 '#111111' | Out-Null
add-text $slide 34 54 527 18 'MaatriCare - user-owned pregnancy support records' 10 $false 2 1 '#444444' | Out-Null

# A user parent table feeds user-owned records through a labeled relationship spine.
$spine = $slide.Shapes.AddConnector(1, 297.5, 151, 297.5, 704)
$spine.Line.ForeColor.RGB = rgb '#566E68'; $spine.Line.Weight = 1.5
add-entity $slide 180 88 'users' 'id PK | email UNIQUE | timezone_id'
$rows = @(
    @('pregnancy_profiles','user_id FK UNIQUE | due_date','1:1'),
    @('appointments','id PK | user_id FK | starts_at','1:N'),
    @('care_tasks','id PK | user_id FK | task_detail_id FK','1:N'),
    @('daily_wellbeing','user_id FK | entry_date UNIQUE','1:N'),
    @('symptom_entries','id PK | user_id FK | severity','1:N'),
    @('pregnancy_milestones','id PK | user_id FK | week_number','1:N'),
    @('emergency_contacts','id PK | user_id FK | contact_type','1:N'),
    @('report_shares','id PK | user_id FK | expires_at','1:N'),
    @('push_subscriptions','id PK | user_id FK | endpoint_hash','1:N'),
    @('push_reminders','id PK | user_id FK | scheduled_at | status','1:N')
)
for ($i = 0; $i -lt $rows.Count; $i++) {
    $row = $rows[$i]
    $column = $i % 2
    $level = [Math]::Floor($i / 2)
    $x = if ($column -eq 0) { 34.0 } else { 326.0 }
    $y = 188.0 + ($level * 102.0)
    $centerY = $y + 31
    $connector = if ($column -eq 0) {
        $slide.Shapes.AddConnector(1, 269, $centerY, 297.5, $centerY)
    } else {
        $slide.Shapes.AddConnector(1, 297.5, $centerY, 326, $centerY)
    }
    $connector.Line.ForeColor.RGB = rgb '#566E68'; $connector.Line.Weight = 1
    add-entity $slide $x $y $row[0] $row[1]
    add-text $slide 276 ($centerY - 9) 42 14 $row[2] 7 $false 2 1 '#444444' | Out-Null
}
# Appointment questions belong to an appointment, and task details are shared definitions.
add-entity $slide 34 698 'appointment_questions' 'id PK | appointment_id FK | answer'
add-entity $slide 326 698 'task_details' 'id PK | title | is_shared'
$questionLink = $slide.Shapes.AddConnector(1, 150, 250, 150, 698)
$questionLink.Line.ForeColor.RGB = rgb '#8A9D97'; $questionLink.Line.DashStyle = 2
add-text $slide 38 668 235 18 'appointment_questions belongs to appointments' 7 $false 1 1 '#555555' | Out-Null
add-text $slide 326 668 235 18 'care_tasks references task_details' 7 $false 1 1 '#555555' | Out-Null

$doc.SaveAs($editable, 24)
$doc.SaveAs($out, 32)
$doc.Close()
$app.Quit()
Get-Item $editable, $out | Select-Object Name, Length, LastWriteTime
