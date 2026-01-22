use strict;
use warnings;
use File::Path qw(make_path);

my $dir = "app/src/main/res/drawable";
make_path($dir);

my %files = (

"ic_play_circle.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,16.5v-9l6,4.5-6,4.5z"/>
</vector>
XML

"ic_pause_circle.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,16h-2V8h2v8zm6,0h-2V8h2v8z"/>
</vector>
XML

"ic_rew.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M11,18l-7,-6 7,-6v12zM20,18l-7,-6 7,-6v12z"/>
</vector>
XML

"ic_ffwd.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M13,6l7,6 -7,6V6zM4,6l7,6 -7,6V6z"/>
</vector>
XML

"ic_prev.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M6,6h2v12H6zM20,6l-10,6 10,6V6z"/>
</vector>
XML

"ic_audio.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M3,10v4h4l5,5V5L7,10H3z"/>
</vector>
XML

"ic_cc.xml" => <<'XML',
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M21,6c0-1.1-0.9-2-2-2H5c-1.1,0-2,0.9-2,2v12c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V6z"/>
</vector>
XML
);

for my $file (keys %files) {
    open my $fh, ">", "$dir/$file" or die "Cannot write $file";
    print $fh $files{$file};
    close $fh;
    print "Created $dir/$file\n";
}

print "\nAll VideoRental icons created successfully.\n";
