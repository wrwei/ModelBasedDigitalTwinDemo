import bpy

for object in bpy.context.scene.objects.values():
    bpy.ops.object.select_all(action='DESELECT')

    object.select_set(True)
    bpy.ops.object.origin_set(type='ORIGIN_GEOMETRY')
