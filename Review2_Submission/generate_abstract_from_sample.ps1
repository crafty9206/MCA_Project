$ErrorActionPreference = 'Stop'
$root = 'C:\project\MCA_Project\Review2_Submission'
$samplePath = Join-Path $root 'R2-MCA_Project_Jan24-91241160033-RAM.pptx'
$previewPptx = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract_Source.pptx'
$previewPdf = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract.pdf'

function Rgb([string]$hex) {
    $value = $hex.TrimStart('#')
    return ([Convert]::ToInt32($value.Substring(0,2),16) +
        ([Convert]::ToInt32($value.Substring(2,2),16) -shl 8) +
        ([Convert]::ToInt32($value.Substring(4,2),16) -shl 16))
}
function Add-Line($slide,[double]$x1,[double]$y1,[double]$x2,[double]$y2,[string]$color='#1A1A1A',[double]$weight=1) {
    $line=$slide.Shapes.AddConnector(1,$x1,$y1,$x2,$y2)
    $line.Line.ForeColor.RGB=Rgb $color
    $line.Line.Weight=$weight
    return $line
}
function Add-Text($slide,[double]$x,[double]$y,[double]$width,[double]$height,[string]$value,[double]$size=12,[bool]$bold=$false,[int]$alignment=1,[double]$lineSpacing=1.5,[string]$color='#111111') {
    $shape=$slide.Shapes.AddTextbox(1,$x,$y,$width,$height)
    $shape.TextFrame.WordWrap=-1
    $shape.TextFrame.AutoSize=0
    $shape.TextFrame.MarginLeft=0
    $shape.TextFrame.MarginRight=0
    $shape.TextFrame.MarginTop=0
    $shape.TextFrame.MarginBottom=0
    $range=$shape.TextFrame.TextRange
    $range.Text=$value.Replace('\n',[Environment]::NewLine)
    $range.Font.Name='Times New Roman'
    $range.Font.Size=$size
    $range.Font.Bold=$bold
    $range.Font.Color.RGB=(Rgb $color)
    $range.ParagraphFormat.Alignment=$alignment
    $range.ParagraphFormat.SpaceWithin=$lineSpacing
    $shape.Width=$width
    $shape.Height=$height
    return $shape
}
function Add-KareHeader($slide,$sourceShape) {
    $sourceShape.Copy()
    $pasted=$slide.Shapes.Paste().Item(1)
    $pasted.LockAspectRatio=-1
    $pasted.Left=40
    $pasted.Top=13
    $pasted.Width=515
    Add-Text $slide 35 151 525 20 'CENTRE FOR DISTANCE AND ONLINE EDUCATION (CDOE)' 12 $true 2 1 '#111111' | Out-Null
    Add-Line $slide 40 176 555 176 '#555555' 0.8 | Out-Null
}
function Add-Entity($slide,[double]$x,[double]$y,[double]$width,[string]$name,[string]$fields) {
    $shape=$slide.Shapes.AddShape(1,$x,$y,$width,49)
    $shape.Fill.ForeColor.RGB=Rgb '#FFFFFF'
    $shape.Line.ForeColor.RGB=Rgb '#333333'
    $shape.Line.Weight=0.8
    $header=$slide.Shapes.AddShape(1,$x,$y,$width,18)
    $header.Fill.ForeColor.RGB=Rgb '#E9EEF0'
    $header.Line.ForeColor.RGB=Rgb '#333333'
    $header.Line.Weight=0.8
    Add-Text $slide ($x+5) ($y+2) ($width-10) 14 $name 9 $true 1 1 '#111111' | Out-Null
    Add-Text $slide ($x+5) ($y+21) ($width-10) 24 $fields 8 $false 1 1 '#111111' | Out-Null
}

$app=New-Object -ComObject PowerPoint.Application
$app.Visible=-1
$sample=$app.Presentations.Open($samplePath,$true,$false,$false)
$logoShape=$sample.Slides.Item(1).Shapes.Item(3)
$doc=$app.Presentations.Add()
$doc.PageSetup.SlideSize=7
$doc.PageSetup.SlideWidth=595
$doc.PageSetup.SlideHeight=842

# Page 1: one-page abstract using the exact institutional header from the sample PPT.
$slide=$doc.Slides.Add(1,12)
$slide.FollowMasterBackground=0
$slide.Background.Fill.ForeColor.RGB=Rgb '#FFFFFF'
Add-KareHeader $slide $logoShape
Add-Text $slide 42 184 300 22 'Name of the Learner: Deepak Kumar' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 330 184 223 22 'Register number: 9224116032' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 42 211 300 22 'Name of the Programme: MCA' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 330 211 223 22 'Semester: IV' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 42 238 300 22 'Name of the Course: Project Work' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 330 238 223 22 'Batch: Jan 2024' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 42 265 300 22 'Course Code: 223MCA4201' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 330 265 223 22 'Regulation: 2022' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 42 292 510 24 'Project Title: MaatriCare' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 42 324 510 22 'Abstract' 14 $true 2 1 '#111111' | Out-Null
$abstract='Pregnancy involves regular appointments, daily care, personal records, and communication with healthcare professionals. MaatriCare is an authenticated web application that brings these activities into one workspace. It supports pregnancy profiles, appointments and visit questions, daily wellbeing, a private symptom journal, milestones, educational resources, summaries, emergency contacts, and reminders. Users control report content and may share selected summaries through revocable, time-limited links. The React and TypeScript frontend uses a Java Spring Boot REST backend with JWT security, JPA, and relational storage (MySQL locally; PostgreSQL configuration). Optional Web Push uses Asia/Kolkata schedules and requires VAPID keys and HTTPS. MaatriCare organizes user-entered information; it does not diagnose, recommend treatment, or replace professional or emergency care.'
Add-Text $slide 48 353 499 206 $abstract 12 $false 4 1.5 '#111111' | Out-Null
Add-Text $slide 42 579 510 20 'Tools & Technologies Used' 12 $true 1 1 '#111111' | Out-Null
Add-Text $slide 53 603 494 72 'Frontend (Design): React, TypeScript, Vite\nBackend (API): Java, Spring Boot REST\nDatabase: MySQL (local), PostgreSQL configuration\nSecurity / Testing: Spring Security, JWT, H2' 12 $false 1 1.5 '#111111' | Out-Null
Add-Text $slide 42 692 510 20 'Approval Committee' 12 $true 2 1 '#111111' | Out-Null
Add-Line $slide 42 756 190 756 '#333333' 0.8 | Out-Null
Add-Line $slide 222 756 376 756 '#333333' 0.8 | Out-Null
Add-Line $slide 405 756 553 756 '#333333' 0.8 | Out-Null
Add-Text $slide 35 763 160 24 'Internal Examiner' 9 $false 2 1 '#111111' | Out-Null
Add-Text $slide 213 763 170 24 'Deputy Director - CDOE' 9 $false 2 1 '#111111' | Out-Null
Add-Text $slide 400 763 160 24 'Director - CDOE' 9 $false 2 1 '#111111' | Out-Null

# Page 2: ER diagram on the same institutional header.
$slide=$doc.Slides.Add(2,12)
$slide.FollowMasterBackground=0
$slide.Background.Fill.ForeColor.RGB=Rgb '#FFFFFF'
Add-KareHeader $slide $logoShape
Add-Text $slide 40 187 515 24 'ENTITY RELATIONSHIP DIAGRAM (ER)' 14 $true 2 1 '#111111' | Out-Null
Add-Text $slide 40 213 515 16 'MaatriCare - simplified implemented relational schema' 9 $false 2 1 '#333333' | Out-Null

$spineX=297.5
Add-Entity $slide 182.5 240 230 'users' 'id PK | email UNIQUE | timezone_id'
Add-Line $slide $spineX 289 $spineX 657 '#444444' 1.1 | Out-Null
$rows=@(
    [pscustomobject]@{Name='pregnancy_profiles';Fields='id PK | user_id FK UNIQUE';Side='L';Relation='1:1'},
    [pscustomobject]@{Name='appointments';Fields='id PK | user_id FK | starts_at';Side='R';Relation='1:N'},
    [pscustomobject]@{Name='care_tasks';Fields='id PK | user_id FK | task_detail_id FK';Side='L';Relation='1:N'},
    [pscustomobject]@{Name='daily_wellbeing';Fields='user_id FK | entry_date UNIQUE';Side='R';Relation='1:N'},
    [pscustomobject]@{Name='symptom_entries';Fields='id PK | user_id FK | severity';Side='L';Relation='1:N'},
    [pscustomobject]@{Name='pregnancy_milestones';Fields='id PK | user_id FK | week_number';Side='R';Relation='1:N'},
    [pscustomobject]@{Name='emergency_contacts';Fields='id PK | user_id FK | type';Side='L';Relation='1:N'},
    [pscustomobject]@{Name='report_shares';Fields='id PK | user_id FK | expires_at';Side='R';Relation='1:N'},
    [pscustomobject]@{Name='push_subscriptions';Fields='id PK | user_id FK | endpoint_hash';Side='L';Relation='1:N'},
    [pscustomobject]@{Name='push_reminders';Fields='id PK | user_id FK | scheduled_at';Side='R';Relation='1:N'},
    [pscustomobject]@{Name='appointment_questions';Fields='id PK | appointment_id FK';Side='L';Relation='1:N*'},
    [pscustomobject]@{Name='task_details';Fields='id PK | title | is_shared';Side='R';Relation='shared'}
)
for($index=0;$index -lt $rows.Count;$index++) {
    $entity=$rows[$index]
    $row=[Math]::Floor($index/2)
    $y=302+($row*58)
    if($entity.Side -eq 'L') {
        $x=35.0
        Add-Line $slide 265 ($y+24.5) $spineX ($y+24.5) '#555555' 0.8 | Out-Null
        Add-Text $slide 267 ($y+8) 28 13 $entity.Relation 6.5 $false 2 1 '#444444' | Out-Null
    } else {
        $x=330.0
        Add-Line $slide $spineX ($y+24.5) 330 ($y+24.5) '#555555' 0.8 | Out-Null
        Add-Text $slide 300 ($y+8) 28 13 $entity.Relation 6.5 $false 2 1 '#444444' | Out-Null
    }
    Add-Entity $slide $x $y 230 $entity.Name $entity.Fields
}
Add-Text $slide 42 668 510 58 'Relationships: one user has one pregnancy profile and may have many appointments, tasks, daily records, symptoms, milestones, report shares, push subscriptions, and reminders. Appointment questions reference appointments; care tasks reference shared task details.' 9 $false 1 1 '#222222' | Out-Null

$doc.SaveAs($previewPptx,24)
$doc.SaveAs($previewPdf,32)
$doc.Close()
$sample.Close()
$app.Quit()
Get-Item $previewPptx,$previewPdf | Select-Object Name,Length,LastWriteTime
